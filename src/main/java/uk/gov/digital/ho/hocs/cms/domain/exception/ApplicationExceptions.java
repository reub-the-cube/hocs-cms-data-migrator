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
}
