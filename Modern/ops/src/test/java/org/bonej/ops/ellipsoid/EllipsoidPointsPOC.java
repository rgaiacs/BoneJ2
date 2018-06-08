
package org.bonej.ops.ellipsoid;

import java.util.Collection;
import java.util.List;

import net.imagej.ImageJ;

import org.scijava.vecmath.Vector3d;

/**
 * A simple class to demonstrate the proof of concept for
 * {@link EllipsoidPoints}.
 * <p>
 * Prints random points in CSV format that can visualised to inspect the
 * distribution.
 * </p>
 * 
 * @author Richard Domander
 */
public final class EllipsoidPointsPOC {

	private EllipsoidPointsPOC() {}

	public static void main(final String[] args) {
		final ImageJ imageJ = new ImageJ();
		@SuppressWarnings("unchecked")
		final Collection<Vector3d> points = (Collection<Vector3d>) imageJ.op().run(
				EllipsoidPoints.class, new double[]{1, 2, 3}, 10_000);
		points.stream().map(Vector3d::toString).forEach(System.out::println);
	}
}
