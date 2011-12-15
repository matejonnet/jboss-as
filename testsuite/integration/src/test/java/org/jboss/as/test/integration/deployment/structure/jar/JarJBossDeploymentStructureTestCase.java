package org.jboss.as.test.integration.deployment.structure.jar;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.logging.Logger;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests parsing of jboss-deployment-structure.xml file in a deployment
 * <p/>
 * User: Jaikiran Pai
 */
@RunWith(Arquillian.class)
public class JarJBossDeploymentStructureTestCase {

    private static final Logger logger = Logger.getLogger(JarJBossDeploymentStructureTestCase.class);

    @EJB(mappedName = "java:module/ClassLoadingEJB")
    private ClassLoadingEJB ejb;

    public static final String TO_BE_FOUND_CLASS_NAME = "org.jboss.as.test.integration.deployment.structure.jar.Available";
    public static final String TO_BE_MISSSING_CLASS_NAME = "org.jboss.as.test.integration.deployment.structure.jar.ToBeIgnored";

    @Deployment
    public static Archive<?> createDeployment() {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "deployment-structure.jar");
        jar.addAsManifestResource(JarJBossDeploymentStructureTestCase.class.getPackage(), "jboss-deployment-structure.xml", "jboss-deployment-structure.xml");

        final JavaArchive jarOne = ShrinkWrap.create(JavaArchive.class, "available.jar");
        jarOne.addClass(Available.class);

        final JavaArchive ignoredJar = ShrinkWrap.create(JavaArchive.class, "ignored.jar");
        ignoredJar.addClass(ToBeIgnored.class);

        jar.addClasses(ClassLoadingEJB.class, JarJBossDeploymentStructureTestCase.class);

        jar.add(jarOne, "a", ZipExporter.class);
        jar.add(ignoredJar, "i", ZipExporter.class);


        logger.info(jar.toString(true));
        return jar;
    }

    /**
     * Make sure the <filter> element in jboss-deployment-structure.xml is processed correctly and the
     * exclude/include is honoured
     *
     * @throws Exception
     */
    @Test
    public void testDeploymentStructureFilters() throws Exception {
        this.ejb.loadClass(TO_BE_FOUND_CLASS_NAME);

        try {
            this.ejb.loadClass(TO_BE_MISSSING_CLASS_NAME);
            Assert.fail("Unexpectedly found class " + TO_BE_MISSSING_CLASS_NAME);
        } catch (ClassNotFoundException cnfe) {
            // expected
        }
    }

    @Test
    public void testUsePhysicalCodeSource() throws ClassNotFoundException {
        Class<?> clazz = this.ejb.loadClass(TO_BE_FOUND_CLASS_NAME);
        Assert.assertTrue( clazz.getProtectionDomain().getCodeSource().getLocation().getProtocol().equals("jar"));
        Assert.assertTrue(ClassLoadingEJB.class.getProtectionDomain().getCodeSource().getLocation().getProtocol().equals("jar"));
    }

}
