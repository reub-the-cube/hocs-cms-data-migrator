package uk.gov.digital.ho.hocs.cms.domain.exception;

public interface ApplicationExceptions {

    class ExtractCaseException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractCaseException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractCaseException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractDocumentException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractDocumentException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractDocumentException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractCorrespondentException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractCorrespondentException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractCorrespondentException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractCaseDataException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractCaseDataException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractCaseDataException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractCompensationDataException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractCompensationDataException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractCompensationDataException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractRiskAssessmentException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractRiskAssessmentException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractRiskAssessmentException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractCaseLinksException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractCaseLinksException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractCaseLinksException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractResponseException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractResponseException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractResponseException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class ExtractCaseHistoryException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public ExtractCaseHistoryException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public ExtractCaseHistoryException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class SendMigrationMessageException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public SendMigrationMessageException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public SendMigrationMessageException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }

    class CreateMigrationDocumentException extends RuntimeException {

        private final LogEvent event;

        private final LogEvent exception;

        public CreateMigrationDocumentException(String msg, LogEvent event, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = null;
        }

        public CreateMigrationDocumentException(String msg, LogEvent event, LogEvent exception, Object... args) {
            super(String.format(msg, args));
            this.event = event;
            this.exception = exception;
        }

        public LogEvent getEvent() {
            return event;
        }

        public LogEvent getException() {return exception;}

    }
}
