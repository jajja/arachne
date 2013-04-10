import org.junit.Test;

import com.jajja.arachne.exceptions.MalformedDomain;
import com.jajja.arachne.net.Domain;

public class DomainTest {
    
    @Test public void mixcase() { // Mixed case.
        assertPublicSuffix("COM", null);
        assertPublicSuffix("example.COM", "example.com");
        assertPublicSuffix("WwW.example.COM", "example.com");       
    }

    @Test public void period() { // Leading dot.
        assertPublicSuffix(".com", null);
        assertPublicSuffix(".example", null);
        assertPublicSuffix(".example.com", null);
        assertPublicSuffix(".example.example", null);       
    }
    
    @Test public void example() { // Unlisted TLD.
        assertPublicSuffix("example", null);
        assertPublicSuffix("example.example", null);
        assertPublicSuffix("b.example.example", null);
        assertPublicSuffix("a.b.example.example", null);
    }
    
    @Test public void local() { // Listed, but non-Internet, TLD.
        assertPublicSuffix("local", null);
        assertPublicSuffix("example.local", null);
        assertPublicSuffix("b.example.local", null);
        assertPublicSuffix("a.b.example.local", null);
    }
    
    @Test public void single() { // TLD with only 1 rule.
        assertPublicSuffix("biz", null);
        assertPublicSuffix("domain.biz", "domain.biz");
        assertPublicSuffix("b.domain.biz", "domain.biz");
        assertPublicSuffix("a.b.domain.biz", "domain.biz");
    }
    
    @Test public void tiers() { // TLD with some 2-level rules.
        assertPublicSuffix("com", null);
        assertPublicSuffix("example.com", "example.com");
        assertPublicSuffix("b.example.com", "example.com");
        assertPublicSuffix("a.b.example.com", "example.com");
        assertPublicSuffix("uk.com", null);
        assertPublicSuffix("example.uk.com", "example.uk.com");
        assertPublicSuffix("b.example.uk.com", "example.uk.com");
        assertPublicSuffix("a.b.example.uk.com", "example.uk.com");
        assertPublicSuffix("test.ac", "test.ac");
    }
    
    @Test public void wildcard() { // TLD with only 1 (wildcard) rule.
        assertPublicSuffix("cy", null);
        assertPublicSuffix("c.cy", null);
        assertPublicSuffix("b.c.cy", "b.c.cy");
        assertPublicSuffix("a.b.c.cy", "b.c.cy");
    }
    
    @Test public void complex() { // More complex TLD.
        assertPublicSuffix("jp", null);
        assertPublicSuffix("test.jp", "test.jp");
        assertPublicSuffix("www.test.jp", "test.jp");
        assertPublicSuffix("ac.jp", null);
        assertPublicSuffix("test.ac.jp", "test.ac.jp");
        assertPublicSuffix("www.test.ac.jp", "test.ac.jp");
        assertPublicSuffix("kyoto.jp", null);
        assertPublicSuffix("c.kyoto.jp", null);
        assertPublicSuffix("b.c.kyoto.jp", "b.c.kyoto.jp");
        assertPublicSuffix("a.b.c.kyoto.jp", "b.c.kyoto.jp");
        assertPublicSuffix("pref.kyoto.jp", "pref.kyoto.jp");   // Exception rule.
        assertPublicSuffix("www.pref.kyoto.jp", "pref.kyoto.jp");   // Exception rule.
        assertPublicSuffix("city.kyoto.jp", "city.kyoto.jp");   // Exception rule.
        assertPublicSuffix("www.city.kyoto.jp", "city.kyoto.jp");   // Exception rule.
    }
    
    @Test public void exceptions() { // TLD with a wildcard rule and exceptions.
        assertPublicSuffix("om", null);
        assertPublicSuffix("test.om", null);
        assertPublicSuffix("b.test.om", "b.test.om");
        assertPublicSuffix("a.b.test.om", "b.test.om");
        assertPublicSuffix("songfest.om", "songfest.om");
        assertPublicSuffix("www.songfest.om", "songfest.om");
    }
    
    @Test public void usk12() { // US K12.
        assertPublicSuffix("us", null);
        assertPublicSuffix("test.us", "test.us");
        assertPublicSuffix("www.test.us", "test.us");
        assertPublicSuffix("ak.us", null);
        assertPublicSuffix("test.ak.us", "test.ak.us");
        assertPublicSuffix("www.test.ak.us", "test.ak.us");
        assertPublicSuffix("k12.ak.us", null);
        assertPublicSuffix("test.k12.ak.us", "test.k12.ak.us");
        assertPublicSuffix("www.test.k12.ak.us", "test.k12.ak.us");
    }

    static void assertPublicSuffix(String name, String entry) {
        boolean isPassed = false;
        try {
            Domain domain = new Domain(name);
            boolean isNull = entry == null && domain.getRecord() == null;
            boolean isEqual = entry != null && domain.getRecord() != null && entry.equals(domain.getRecord().getEntry());
            isPassed = isNull || isEqual;
            if (!isPassed) {
                System.out.println("Fail! " + name + " => " + domain.getRecord().getEntry() + " = " + entry);
            }
        } catch (MalformedDomain e) {
            isPassed = entry == null;
            if (!isPassed) {
                System.out.println("Fail! " + e.getMessage() + " (" + e.getDomain() + ")");                
            }
        }
        assert(isPassed);
    }
}
