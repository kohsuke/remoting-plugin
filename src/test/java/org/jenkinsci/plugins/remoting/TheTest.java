package org.jenkinsci.plugins.remoting;

import hudson.remoting.Channel;
import hudson.remoting.Which;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class TheTest {
    @Rule
    public final JenkinsRule j = new JenkinsRule();

    /**
     * Ensures that the unit test is indeed loading overridden remoting
     */
    @Test
    public void test() throws Exception {
        String version = getExpectedVersion();

             // see which jar file we are using?
        try (JarFile jar = new JarFile(Which.jarFile(Channel.class))) {
            ZipEntry e = jar.getEntry("META-INF/MANIFEST.MF");
            try (InputStream is = jar.getInputStream(e)) {
                Attributes mf = new Manifest(is).getMainAttributes();
                assertThat(mf.getValue("Version"),is(version));
            }
        }

        // see which plugin is installed?
        assertThat(trimSnapshot(j.jenkins.getPlugin("remoting").getWrapper().getVersion()), is(version));
    }

    /**
     * Determines the expected version of remoting.jar and this plugin.
     */
    private String getExpectedVersion() throws Exception {
        /*
        // doesn't seem to be available during 'mvn test'
        try (InputStream is = new FileInputStream("target/remoting/META-INF/MANIFEST.MF")) {
            Attributes mf = new Manifest(is).getMainAttributes();
            return mf.getValue("Implementation-Version");
        }*/
        Document pom = new SAXReader().read(new File("pom.xml"));
        return pom.getRootElement().element("version").getText();
    }

    /**
     * For version number like "1.0-SNAPSHOT (private-921e74fb-kohsuke)" trim off the whitespace and onward
     */
    private String trimSnapshot(String v) {
        return v.split(" ")[0];
    }
}

