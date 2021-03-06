package notifiers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

import com.sun.org.apache.xml.internal.utils.UnImplNode;

import play.Logger;
import play.Play;
import play.libs.Mail;
import play.mvc.Http.Header;
import play.mvc.Mailer;

public class SystemEmail extends Mailer {
    public static final String ERROR_EMAIL = Play.configuration.getProperty("piazza.error_email");
    
    public static class EmailedException implements Iterable<EmailedException> {
        public final Throwable t;
        public EmailedException(Throwable t) {
            this.t = t;
        }

        private static class Iter implements Iterator<EmailedException> {
            private Throwable current;
            
            public Iter(Throwable t) { 
                current = t;
            }

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public EmailedException next() {
                EmailedException ret = new EmailedException(current);
                current = current.getCause();
                return ret;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public Iterator<EmailedException> iterator() {
            return new Iter(this.t);
        }
        
        /**
         * @return formatted stack trace for exception
         */
        public String getStackTrace() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            this.t.printStackTrace(pw);
            return sw.toString();
        }
    }

    public static void logError(String username, Throwable e, Collection<Header> headers) {
        Logger.error(e, "Sending Gack to %s", ERROR_EMAIL);
        Date date = new Date();
        setFrom("piazza@heroku.com");
        addRecipient(ERROR_EMAIL);
        setSubject(String.format("Error at %s logged: %s", date, e.getClass()));
        EmailedException errors = new EmailedException(e);
        send(errors, username, headers, date);
    }
}
