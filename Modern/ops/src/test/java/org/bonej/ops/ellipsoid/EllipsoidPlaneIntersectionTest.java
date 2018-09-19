/*
BSD 2-Clause License
Copyright (c) 2018, Michael Doube, Richard Domander, Alessandro Felder
All rights reserved.
Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
* Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package org.bonej.ops.ellipsoid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.imagej.ImageJ;
import net.imagej.ops.special.function.BinaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imglib2.util.ValuePair;

import org.apache.commons.math3.random.RandomVectorGenerator;
import org.apache.commons.math3.random.UnitSphereRandomVectorGenerator;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.junit.AfterClass;
import org.junit.Test;


/**
 * Tests for {@link EllipsoidPlaneIntersection}
 *
 * @author Alessandro Felder
 */
public class EllipsoidPlaneIntersectionTest {
    private static final ImageJ IMAGE_J = new ImageJ();

    @AfterClass
    public static void oneTimeTearDown() {
        IMAGE_J.context().dispose();
    }

    private static final RandomVectorGenerator sphereRNG = new UnitSphereRandomVectorGenerator(3);
    @SuppressWarnings("unchecked")
	private final BinaryFunctionOp<Ellipsoid, ValuePair<Vector3dc, Vector3dc>, List<Vector3d>> intersectionOp =
		(BinaryFunctionOp) Functions.binary(IMAGE_J.op(),
			EllipsoidPlaneIntersection.class, List.class, Ellipsoid.class,
			ValuePair.class);

    @Test
    public void testGeneralEllipsoidAndPlane() {
        final Ellipsoid ellipsoid = new Ellipsoid(1,2,3);

        final double oneOverSqrtThree = 1.0/Math.sqrt(3.0);
        final Vector3dc firstAxis = new Vector3d(new Vector3d(oneOverSqrtThree,oneOverSqrtThree,oneOverSqrtThree));
        final Vector3d secondAxis = new Vector3d(new Vector3d(-oneOverSqrtThree,0,oneOverSqrtThree));
        secondAxis.normalize();
        final Vector3d thirdAxis = new Vector3d();
        firstAxis.cross(secondAxis, thirdAxis);


        final Matrix3d orientation = new Matrix3d();
        orientation.setColumn(0, firstAxis);
        orientation.setColumn(1, secondAxis);
        orientation.setColumn(2, thirdAxis);

        ellipsoid.setOrientation(orientation);


        final ValuePair<Vector3dc,Vector3dc> plane = new ValuePair<>(new Vector3d(0,0,0.5),new Vector3d(0,0,1));

        final List<Vector3d> intersectionEllipse = intersectionOp.calculate(ellipsoid, plane);

        assertEquals(3, intersectionEllipse.size());

        final Vector3d ellipseCentre = intersectionEllipse.get(0);
        final Vector3d ellipseAxisA = intersectionEllipse.get(1);
        final Vector3d ellipseAxisB = intersectionEllipse.get(2);
        assertEquals(ellipseAxisA.dot(ellipseAxisB),0.0, 1.0e-12);

        final Vector3d pointOnPlaneAndEllipsoidA = new Vector3d(ellipseCentre);
        pointOnPlaneAndEllipsoidA.add(ellipseAxisA);
        final Vector3d pointOnPlaneAndEllipsoidB = new Vector3d(ellipseCentre);
        pointOnPlaneAndEllipsoidB.add(ellipseAxisB);

        assertTrue(fulfillsPlaneEquation(pointOnPlaneAndEllipsoidA, plane));
        assertTrue(fulfillsEllipsoidEquation(pointOnPlaneAndEllipsoidA, ellipsoid));
        assertTrue(fulfillsPlaneEquation(pointOnPlaneAndEllipsoidB, plane));
        assertTrue(fulfillsEllipsoidEquation(pointOnPlaneAndEllipsoidB, ellipsoid));
    }

    @Test
    public void testAxisAlignedEllipsoidWithObliquePlane() {
        final Ellipsoid axisAligned = new Ellipsoid(1,2,3);
        final double oneOverSqrtThree = 1.0/Math.sqrt(3.0);
        final ValuePair<Vector3dc,Vector3dc> obliquePlane = new ValuePair<>(new Vector3d(0,0,0.5),new Vector3d(oneOverSqrtThree,oneOverSqrtThree,oneOverSqrtThree));

        final EllipsoidPlaneIntersection intersection = new EllipsoidPlaneIntersection();
        final List<Vector3d> axisAlignedIntersectionEllipse = intersection.findAxisAlignedCentredIntersectionEllipse(new Vector3d(axisAligned.getA(), axisAligned.getB(), axisAligned.getC()), obliquePlane);

        assertEquals(3, axisAlignedIntersectionEllipse.size());

        final Vector3d ellipseCentre = axisAlignedIntersectionEllipse.get(0);
        final Vector3d ellipseAxisA = axisAlignedIntersectionEllipse.get(1);
        final Vector3d ellipseAxisB = axisAlignedIntersectionEllipse.get(2);
        assertEquals(ellipseAxisA.dot(ellipseAxisB),0.0, 1.0e-12);

        final Vector3d pointOnPlaneAndEllipsoidA = new Vector3d(ellipseCentre);
        pointOnPlaneAndEllipsoidA.add(ellipseAxisA);
        final Vector3d pointOnPlaneAndEllipsoidB = new Vector3d(ellipseCentre);
        pointOnPlaneAndEllipsoidB.add(ellipseAxisB);

        assertTrue(fulfillsPlaneEquation(pointOnPlaneAndEllipsoidA, obliquePlane));
        assertTrue(fulfillsEllipsoidEquation(pointOnPlaneAndEllipsoidA, axisAligned));
        assertTrue(fulfillsPlaneEquation(pointOnPlaneAndEllipsoidB, obliquePlane));
        assertTrue(fulfillsEllipsoidEquation(pointOnPlaneAndEllipsoidB, axisAligned));
    }

    @Test
    public void testAxisAlignedEllipsoidWithParallelPlane() {
        final Ellipsoid axisAligned = new Ellipsoid(1,2,3);
        final ValuePair<Vector3dc,Vector3dc> XYPlane = new ValuePair<>(new Vector3d(0,0,0.25),new Vector3d(0,0,1));

        final EllipsoidPlaneIntersection intersection = new EllipsoidPlaneIntersection();
        final List<Vector3d> axisAlignedIntersectionEllipse = intersection.findAxisAlignedCentredIntersectionEllipse(new Vector3d(axisAligned.getA(), axisAligned.getB(), axisAligned.getC()), XYPlane);

        assertEquals(3, axisAlignedIntersectionEllipse.size());

        final Vector3d ellipseCentre = axisAlignedIntersectionEllipse.get(0);
        assertEquals(ellipseCentre.x, 0, 1.0e-12);
        assertEquals(ellipseCentre.y, 0, 1.0e-12);
        assertEquals(ellipseCentre.z, XYPlane.getA().z(), 1.0e-12);

        final Vector3d ellipseAxisA = axisAlignedIntersectionEllipse.get(1);
        final Vector3d ellipseAxisB = axisAlignedIntersectionEllipse.get(2);
        assertEquals(ellipseAxisA.dot(ellipseAxisB),0.0, 1.0e-12);

        final Vector3d pointOnPlaneAndEllipsoidA = new Vector3d(ellipseCentre);
        pointOnPlaneAndEllipsoidA.add(ellipseAxisA);
        final Vector3d pointOnPlaneAndEllipsoidB = new Vector3d(ellipseCentre);
        pointOnPlaneAndEllipsoidB.add(ellipseAxisB);

        assertTrue(fulfillsPlaneEquation(pointOnPlaneAndEllipsoidA, XYPlane));
        assertTrue(fulfillsEllipsoidEquation(pointOnPlaneAndEllipsoidA, axisAligned));
        assertTrue(fulfillsPlaneEquation(pointOnPlaneAndEllipsoidB, XYPlane));
        assertTrue(fulfillsEllipsoidEquation(pointOnPlaneAndEllipsoidB, axisAligned));

    }


    private boolean fulfillsEllipsoidEquation(final Vector3dc point, final Ellipsoid ellipsoid) {
        final Matrix3d Q = new Matrix3d();
        ellipsoid.getOrientation().get3x3(Q);

        //A = Q*A*Q^T
        final Matrix3d D = new Matrix3d();
        D.scaling(1.0/(ellipsoid.getA()*ellipsoid.getA()),1.0/(ellipsoid.getB()*ellipsoid.getB()),1.0/(ellipsoid.getC()*ellipsoid.getC()));
        final Matrix3d A = new Matrix3d();
        Q.mul(D,A);

        final Matrix3d QT = Q.transpose();
        A.mul(QT,A);

        final Vector3d pRelativeToCentre = ellipsoid.getCentroid();
        pRelativeToCentre.mul(-1.0);
        pRelativeToCentre.add(point);

        final Vector3d Ap = new Vector3d(pRelativeToCentre);
        A.transform(Ap);

        return approximatelyEqual(pRelativeToCentre.dot(Ap),1.0,1.0e-12);
    }

    private boolean fulfillsPlaneEquation(final Vector3dc point, final ValuePair<Vector3dc, Vector3dc> plane) {
        return approximatelyEqual(plane.getB().dot(point), plane.getA().dot(plane.getB()),1.0e-12);
    }

    @Test
    public void testGenerationOfBasis() {
        for(int i=0; i<10; i++) {
            final double[] randomVector = sphereRNG.nextVector();
            final Vector3d planeNormal = new Vector3d(randomVector[0], randomVector[1], randomVector[2]);
            planeNormal.normalize();

            final Vector3dc axisLengths = new Vector3d(1, 2, 3);
            assertTrue(basisConforms(axisLengths, planeNormal));
        }
    }

    private boolean basisConforms(final Vector3dc semiAxisLengths, final Vector3dc n)
    {
        final List<Vector3dc> basis = EllipsoidPlaneIntersection.completeBasis(semiAxisLengths,n);
        final Vector3dc r = basis.get(0);
        final Vector3dc s = basis.get(1);

        final double a = semiAxisLengths.x();
        final double b = semiAxisLengths.y();
        final double c = semiAxisLengths.z();

        final double tol = 1.0e-12;

        final boolean basisVectorsUnitized = approximatelyEqual(n.length(),1.0, tol) && approximatelyEqual(r.length(), 1.0, tol) && approximatelyEqual(s.length(),1.0,tol);
        final boolean basisVectorsOrthogonal = approximatelyEqual(r.dot(n), 0.0, tol) && approximatelyEqual(s.dot(n), 0.0, tol) && approximatelyEqual(r.dot(s), 0.0, tol);
        final boolean equation7fulfilled = approximatelyEqual(r.x() * s.x() / (a * a) + r.y() * s.y() / (b * b) + r.z() * s.z() / (c * c),0.0, tol);

        return  basisVectorsUnitized && basisVectorsOrthogonal && equation7fulfilled;
    }

    private boolean approximatelyEqual(final double one, final double two, final double tol)
    {
        return (Math.abs(one-two)<tol);
    }
}