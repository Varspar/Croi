package com.croi.voice.service;

import com.croi.appointment.entity.CallRecording;
import com.croi.appointment.entity.ConversationTurn;
import com.croi.appointment.repository.CallRecordingRepository;
import com.croi.appointment.repository.ConversationTurnRepository;
import com.croi.common.constants.ErrorMessages;
import com.croi.common.exception.ErrorCode;
import com.croi.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

/**
 * Split into a fast synchronous half (DB row + turns — the caller needs the
 * generated id back immediately, for VoiceCallResponse.callId) and a slow async
 * half (writing the audio file to disk). The spec's "@Async, don't block on file
 * writes" only makes sense for the file I/O — making the whole save async would
 * mean the HTTP response couldn't include the call id it's supposed to return.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CallRecordingService {

    private final CallRecordingRepository callRecordingRepository;
    private final ConversationTurnRepository conversationTurnRepository;

    @Value("${recordings.directory}")
    private String recordingsDirectory;

    @Transactional
    public CallRecording createCallRecording(UUID organizationId, UUID agentId, String callerId,
                                              Instant callStart, String callerTranscript, String agentResponse) {
        CallRecording recording = CallRecording.builder()
                .organizationId(organizationId)
                .agentId(agentId)
                .patientPhone(callerId)
                .callStartTime(callStart == null ? LocalDateTime.now() : LocalDateTime.ofInstant(callStart, ZoneOffset.UTC))
                .transcript(callerTranscript)
                .transcriptionStatus(callerTranscript == null || callerTranscript.isBlank() ? "failed" : "completed")
                .build();
        recording = callRecordingRepository.save(recording);

        if (callerTranscript != null && !callerTranscript.isBlank()) {
            conversationTurnRepository.save(ConversationTurn.builder()
                    .callRecordingId(recording.getId()).speaker("caller").message(callerTranscript).build());
        }
        if (agentResponse != null && !agentResponse.isBlank()) {
            conversationTurnRepository.save(ConversationTurn.builder()
                    .callRecordingId(recording.getId()).speaker("agent").message(agentResponse).build());
        }

        return recording;
    }

    /** Writes the caller's audio to disk and backfills audio_file_path/storage_size_bytes. Never blocks the caller. */
    @Async
    @Transactional
    public void persistAudioFileAsync(UUID callRecordingId, UUID agentId, String audioBase64) {
        if (audioBase64 == null || audioBase64.isBlank()) {
            return;
        }

        try {
            byte[] audioBytes = Base64.getDecoder().decode(audioBase64);
            Path dir = Path.of(recordingsDirectory, agentId.toString());
            Files.createDirectories(dir);
            Path file = dir.resolve(System.currentTimeMillis() + ".wav");
            Files.write(file, audioBytes);

            callRecordingRepository.findById(callRecordingId).ifPresent(recording -> {
                recording.setAudioFilePath(file.toString());
                recording.setStorageSizeBytes((long) audioBytes.length);
                callRecordingRepository.save(recording);
            });
        } catch (IllegalArgumentException ex) {
            log.error("Could not save call recording audio for {}: audioBase64 was not valid base64", callRecordingId, ex);
        } catch (IOException ex) {
            // Metadata (transcript, turns) is already saved — a failed file write only
            // means audio_file_path stays null, not a lost recording.
            log.error("Could not write call recording audio file for {}", callRecordingId, ex);
        }
    }

    @Transactional
    public CallRecording endCall(UUID callId, String outcome, Integer durationSeconds, String notes) {
        CallRecording recording = callRecordingRepository.findById(callId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CALL_RECORDING_NOT_FOUND, ErrorMessages.CALL_RECORDING_NOT_FOUND));

        recording.setCallEndTime(LocalDateTime.now());
        if (durationSeconds != null) recording.setDurationSeconds(durationSeconds);
        if (outcome != null) recording.setOutcome(outcome);
        if (notes != null) recording.setOutcomeNotes(notes);

        return callRecordingRepository.save(recording);
    }
}
