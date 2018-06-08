
package org.bonej.ops.ita;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.bonej.ops.ita.NPoint.VectorsAngle;
import org.junit.Test;
import org.scijava.vecmath.Vector3d;

public class NPointTest {

	@Test
	public void testVectorsAngleConstructor() {
		final VectorsAngle va = new VectorsAngle(null, null);
		assertNotNull(va.getU());
		assertNotNull(va.getV());
		assertTrue(Double.isNaN(va.getAngle()));
	}

	@Test
	public void testVectorsAngleSetters() {
		final Vector3d u = new Vector3d(1, 0, 0);
		final Vector3d v = new Vector3d(0, 1, 0);
		final VectorsAngle va = new VectorsAngle(u, v);
		final double angle = va.getAngle();

		va.setU(null);
		assertEquals(u, va.getU());

		va.setV(null);
		assertEquals(v, va.getV());

		assertEquals(angle, va.getAngle(), 1e-12);
	}

}
