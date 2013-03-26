/**
 * Copyright 2013 Peergreen S.A.S.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.peergreen.example.arquillian.bundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.Collection;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.osgi.metadata.OSGiManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogService;

/**
 * Deploy the bundle and check if all is fine with the deployment on Peergreen platform.
 * @author Florent Benoit
 */
@RunWith(Arquillian.class)
public class TestWithJUnitMyBundle {
	
	/**
	 * Symbolic Name.
	 */
	private static final String SYMBOLIC_NAME = "my-bundle";
	
	/**
	 * LDAP filter for selecting our implementation of the log service.
	 */
	private static final String FILTER = "(&(" + Constants.OBJECTCLASS + "=" + LogService.class.getName() + ")(nature=basic))";

	/**
	 * Injects the bundle context.
	 */
	@ArquillianResource
	public BundleContext bundleContext;

	/**
	 * Injects the bundle.
	 */
	@ArquillianResource
	public Bundle bundle;

	/**
	 * Creates a new Java Archive.
	 * @return the archive
	 */
	@Deployment
	public static JavaArchive createdeployment() {
		final JavaArchive archive = ShrinkWrap.create(JavaArchive.class, "mybundle.jar");
		archive.setManifest(new Asset() {
			public InputStream openStream() {
				// Adds OSGi entries
				OSGiManifestBuilder builder = OSGiManifestBuilder.newInstance();
				builder.addBundleManifestVersion(2);
				builder.addBundleSymbolicName(SYMBOLIC_NAME);
				builder.addBundleVersion("1.0.0");
				
				// Import the LogService package
				builder.addImportPackages(LogService.class);
				
				// Adds an activator
				builder.addBundleActivator(Activator.class.getName());
				return builder.openStream();
			}
		});

		return archive.addClasses(Activator.class, BasicLogService.class);
	}

	@Test
	public void testBundleContextInjection() {
		assertNotNull("BundleContext injected", bundleContext);
	}

	@Test
	public void testBundleInjection() throws Exception {
		assertNotNull("Bundle injected", bundle);
		assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundle.getState());
		bundle.start();
		assertEquals("Bundle ACTIVE", Bundle.ACTIVE, bundle.getState());
	}


	@Test
	public void testSymbolicNameAvailable() throws InvalidSyntaxException {
		assertNotNull(bundleContext);
		Bundle[] bundles = bundleContext.getBundles();
		boolean found = false;
		int b = 0;
		while (b < bundles.length && !found) {
			Bundle bundle = bundles[b];
			String symbolicName = bundle.getSymbolicName();
			if (SYMBOLIC_NAME.equals(symbolicName)) {
				found = true;
				break;
			}
			b++;
		}
		assertTrue(found);
		
	}

	
	@Test
	public void testService() throws InvalidSyntaxException {

		// Gets the service reference
        Collection<ServiceReference<LogService>> logServiceReferences =  bundleContext.getServiceReferences(LogService.class, FILTER);
		assertNotNull("service references is null", logServiceReferences);
		
		// We should have only one reference matching this filter
		assertEquals(1, logServiceReferences.size());

		// takes the first element
		ServiceReference<LogService> logServiceReference = logServiceReferences.iterator().next();
		
        // check service property
        String nature = (String) logServiceReference.getProperty("nature");
        assertNotNull("No nature property on the service reference", nature);
        assertEquals("basic", nature);
		
        // Check that the service is here
		LogService logService = bundleContext.getService(logServiceReference);
		assertNotNull("Log service not Found", logService);

		// use it
		logService.log(LogService.LOG_DEBUG, "test");

	}

	@Test
	public void testStop() throws Exception {
		bundle.stop();
		assertEquals("Bundle RESOLVED", Bundle.RESOLVED, bundle.getState());
	}

}
