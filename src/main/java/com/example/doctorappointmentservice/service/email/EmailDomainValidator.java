package com.example.doctorappointmentservice.service.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.IDN;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Set;

/**
 * Rejects addresses that can never receive mail: RFC 2606/6761 reserved names
 * (example.com, *.test, ...), well-known disposable providers, and domains with
 * no MX/A/AAAA records. This is a first-pass filter only; it can't prove the
 * mailbox exists or belongs to the user, which is what email verification is for.
 * DNS failures other than "domain doesn't exist" fail open so a resolver hiccup
 * never blocks a legitimate signup.
 */
@Component
public class EmailDomainValidator {

    private static final Logger log = LoggerFactory.getLogger(EmailDomainValidator.class);

    private static final Set<String> RESERVED_DOMAINS = Set.of("example.com", "example.net", "example.org", "example.edu");
    private static final Set<String> RESERVED_TLDS = Set.of("test", "example", "invalid", "localhost", "local", "localdomain", "internal");

    /** Best-effort list of common throwaway providers; not exhaustive. */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "guerrillamail.com", "guerrillamail.net", "guerrillamail.org", "sharklasers.com",
            "10minutemail.com", "10minutemail.net", "tempmail.com", "temp-mail.org", "tempmail.net", "tempail.com",
            "yopmail.com", "yopmail.net", "trashmail.com", "trashmail.net", "throwawaymail.com", "getnada.com",
            "maildrop.cc", "dispostable.com", "fakeinbox.com", "mailnesia.com", "mintemail.com", "mytemp.email",
            "moakt.com", "emailondeck.com", "spamgourmet.com", "burnermail.io", "discard.email", "mohmal.com",
            "inboxkitten.com", "tempinbox.com", "mail.tm", "mailcatch.com", "spam4.me", "getairmail.com"
    );

    /**
     * @throws IllegalArgumentException with a user-facing message when the address can't be a real mailbox
     */
    public void assertDeliverable(String normalizedEmail) {
        int at = normalizedEmail.lastIndexOf('@');
        if (at < 1 || at == normalizedEmail.length() - 1) {
            throw new IllegalArgumentException("Invalid email detected");
        }

        String domain;
        try {
            domain = IDN.toASCII(normalizedEmail.substring(at + 1)).toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid email detected");
        }

        if (isReserved(domain)) {
            throw new IllegalArgumentException("Please use a real email address.");
        }
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new IllegalArgumentException("Disposable email addresses aren't allowed. Please use a permanent email.");
        }
        if (!domainCanReceiveMail(domain)) {
            throw new IllegalArgumentException("That email domain doesn't appear to accept mail. Check for typos.");
        }
    }

    private boolean isReserved(String domain) {
        for (String reserved : RESERVED_DOMAINS) {
            if (domain.equals(reserved) || domain.endsWith("." + reserved)) return true;
        }
        String tld = domain.substring(domain.lastIndexOf('.') + 1);
        return RESERVED_TLDS.contains(tld);
    }

    private boolean domainCanReceiveMail(String domain) {
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
        env.put("com.sun.jndi.dns.timeout.initial", "2000");
        env.put("com.sun.jndi.dns.timeout.retries", "1");

        DirContext ctx = null;
        try {
            ctx = new InitialDirContext(env);

            Attribute mx = ctx.getAttributes(domain, new String[]{"MX"}).get("MX");
            if (mx != null && mx.size() > 0) {
                // RFC 7505 "null MX" ("0 .") explicitly means the domain accepts no mail.
                boolean nullMx = mx.size() == 1 && mx.get(0).toString().trim().matches("0\\s+\\.?");
                return !nullMx;
            }

            // No MX: mail falls back to the domain's own address records (RFC 5321 5.1).
            Attributes addr = ctx.getAttributes(domain, new String[]{"A", "AAAA"});
            return addr.get("A") != null || addr.get("AAAA") != null;
        } catch (NameNotFoundException ex) {
            return false;
        } catch (NamingException ex) {
            log.warn("DNS lookup failed for {} ({}); allowing signup", domain, ex.getMessage());
            return true;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException ignored) {
                    // nothing useful to do
                }
            }
        }
    }
}
