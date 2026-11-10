package com.coursehub.service;

import com.coursehub.model.Course;
import com.coursehub.model.QueueEntry;
import com.coursehub.model.Registration;

import com.coursehub.repository.QueueRepository;
import com.coursehub.repository.RegistrationRepository;
import com.coursehub.repository.StudentRepository;
import com.coursehub.repository.CourseRepository;
import com.coursehub.service.RegistrationService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;



public class QueueService {

    private final QueueRepository queueRepository;
    private final RegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RegistrationService registrationService;

    // Default constructor.
    public QueueService() {
        this(
                new QueueRepository(),
                new RegistrationRepository(),
                new StudentRepository(),
                new CourseRepository()
        );
    }

    // Constructor with shared repositories.
    public QueueService(
            QueueRepository queueRepository,
            RegistrationRepository registrationRepository,
            StudentRepository studentRepository,
            CourseRepository courseRepository) {

        this.queueRepository = queueRepository;
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;

        // Use the same repositories as this QueueService.
        this.registrationService = new RegistrationService(
                registrationRepository,
                studentRepository,
                courseRepository
        );
    }

    // CREATE: Add a student to the waiting queue.
    public synchronized QueueEntry joinQueue(
            String studentId,
            String courseCode) {

        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code is required.");
        }

        studentId = studentId.trim();
        courseCode = courseCode.trim();

        // 1. Check whether the student exists.
        if (studentRepository.findById(studentId).isEmpty()) {
            throw new IllegalArgumentException("Student does not exist.");
        }

        // 2. Check whether the course exists.
        Course course = courseRepository.findByCode(courseCode)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course does not exist."));

        // 3. Prevent students who are already registered from joining.
        if (registrationRepository.findActiveRegistration(
                studentId, courseCode).isPresent()) {

            throw new IllegalArgumentException(
                    "Student is already registered for this course.");
        }

        // 4. Prevent duplicate waiting entries.
        final String checkedStudentId = studentId;

        boolean alreadyWaiting = queueRepository
                .findWaitingByCourseCode(courseCode)
                .stream()
                .anyMatch(entry ->
                        entry.getStudentId() != null
                                && entry.getStudentId()
                                .equalsIgnoreCase(checkedStudentId));

        if (alreadyWaiting) {
            throw new IllegalArgumentException(
                    "Student is already waiting for this course.");
        }

        // 5. Check whether the course is full.
        long activeCount = registrationRepository
                .findByCourseCode(courseCode)
                .stream()
                .filter(registration ->
                        Registration.ACTIVE.equalsIgnoreCase(
                                registration.getStatus()))
                .count();

        if (activeCount < course.getCapacity()) {
            throw new IllegalStateException(
                    "The course still has available seats. Register directly.");
        }

        // 6. Calculate the next queue position.
        List<QueueEntry> waitingEntries =
                queueRepository.findWaitingByCourseCode(courseCode);

        int nextPosition = waitingEntries.size() + 1;

        // 7. Create the queue entry.
        QueueEntry entry = new QueueEntry(
                "QUEUE-" + UUID.randomUUID(),
                studentId,
                courseCode,
                LocalDateTime.now(),
                nextPosition,
                QueueEntry.WAITING
        );

        queueRepository.insert(entry);

        return entry;
    }
    // READ: Get all students waiting for a course.
    public List<QueueEntry> getWaitingList(String courseCode) {

        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code is required.");
        }

        if (courseRepository.findByCode(courseCode.trim()).isEmpty()) {
            throw new IllegalArgumentException("Course does not exist.");
        }

        return queueRepository.findWaitingByCourseCode(courseCode.trim());
    }


    // READ: Get a student's position in the waiting queue.
    public int getStudentQueuePosition(
            String studentId,
            String courseCode) {

        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        List<QueueEntry> waitingList = getWaitingList(courseCode);

        return waitingList.stream()
                .filter(entry -> entry.getStudentId() != null
                        && entry.getStudentId().equalsIgnoreCase(studentId.trim()))
                .map(QueueEntry::getPosition)
                .findFirst()
                .orElse(-1);
    }

    // READ: Get a student's queue entry, including its current status.
    public QueueEntry getStudentQueueEntry(
            String studentId,
            String courseCode) {

        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code is required.");
        }

        List<QueueEntry> entries =
                queueRepository.findByStudentId(studentId);

        QueueEntry latestEntry = null;

        for (QueueEntry entry : entries) {

            if (entry.getCourseCode() != null
                    && entry.getCourseCode().equalsIgnoreCase(courseCode.trim())) {

                if (QueueEntry.WAITING.equalsIgnoreCase(entry.getStatus())) {
                    return entry;
                }

                latestEntry = entry;
            }
        }

        return latestEntry;
    }

    // DELETE: Cancel a student's waiting-list request.
    public synchronized boolean cancelQueueRequest(
            String studentId,
            String courseCode) {

        if (studentId == null || studentId.isBlank()) {
            throw new IllegalArgumentException("Student ID is required.");
        }

        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code is required.");
        }

        studentId = studentId.trim();
        courseCode = courseCode.trim();

        final String checkedStudentId = studentId;

        // Find the student's waiting entry.
        List<QueueEntry> waitingList =
                queueRepository.findWaitingByCourseCode(courseCode);

        QueueEntry target = waitingList.stream()
                .filter(entry -> entry.getStudentId() != null
                        && entry.getStudentId().equalsIgnoreCase(checkedStudentId))
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Student is not in the waiting queue."));

        // Change the status to CANCELLED.
        target.setStatus(QueueEntry.CANCELLED);
        target.setPosition(0);

        // Save the updated entry.
        queueRepository.update(target);

        // Update the remaining queue positions.
        updateQueuePositions(courseCode);

        return true;
    }

    private void updateQueuePositions(String courseCode) {

        List<QueueEntry> waitingList =
                queueRepository.findWaitingByCourseCode(courseCode);

        for (int i = 0; i < waitingList.size(); i++) {

            QueueEntry entry = waitingList.get(i);

            entry.setPosition(i + 1);

            queueRepository.update(entry);
        }
    }

    // PROMOTE: Promote the first waiting student when a seat becomes available.
    public synchronized Registration promoteNextStudent(String courseCode) {

        if (courseCode == null || courseCode.isBlank()) {
            throw new IllegalArgumentException("Course code is required.");
        }

        courseCode = courseCode.trim();

        // Get the first student in the waiting queue.
        List<QueueEntry> waitingList =
                queueRepository.findWaitingByCourseCode(courseCode);

        if (waitingList.isEmpty()) {
            return null;
        }

        QueueEntry nextStudent = waitingList.get(0);

        // Register the student for the course.
        Registration registration =
                registrationService.registerStudentForCourse(
                        nextStudent.getStudentId(),
                        courseCode
                );

        // Mark the queue entry as promoted.
        nextStudent.setStatus(QueueEntry.PROMOTED);
        nextStudent.setPosition(0);

        queueRepository.update(nextStudent);

        // Move the remaining students forward.
        updateQueuePositions(courseCode);

        return registration;
    }

}