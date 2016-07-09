package org.bonej.ops.connectivity;

import net.imagej.ImageJ;
import net.imagej.ImgPlus;
import net.imagej.ops.Ops;
import net.imagej.ops.special.function.BinaryFunctionOp;
import net.imagej.ops.special.function.Functions;
import net.imglib2.Cursor;
import net.imglib2.FinalDimensions;
import net.imglib2.img.Img;
import net.imglib2.type.logic.BitType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link Octant Octant} convenience class
 *
 * @author Richard Domander
 */
public class OctantTest {
    private static final ImageJ IMAGE_J = new ImageJ();
    private static BinaryFunctionOp<FinalDimensions, BitType, Img<BitType>> imgCreator;

    @BeforeClass
    public static void oneTimeSetUp() {
        imgCreator = (BinaryFunctionOp) Functions
                .binary(IMAGE_J.op(), Ops.Create.Img.class, Img.class, FinalDimensions.class, new BitType());
    }

    @AfterClass
    public static void oneTimeTearDown() {
        IMAGE_J.context().dispose();
    }

    @Test
    public void testIsNeighborhoodEmpty() throws Exception {
        final Img<BitType> img = imgCreator.compute1(new FinalDimensions(2, 2, 2));
        final ImgPlus<BitType> imgPlus = new ImgPlus<>(img);
        Octant<BitType> octant = new Octant<>(imgPlus);

        octant.setNeighborhood(1, 1, 1);

        assertTrue("Neighborhood should be empty", octant.isNeighborhoodEmpty());

        img.forEach(BitType::setOne);
        octant.setNeighborhood(1, 1, 1);

        assertFalse("Neighborhood should not be empty", octant.isNeighborhoodEmpty());
    }

    @Test
    public void testSetNeighborhood() throws Exception {
        final Img<BitType> img = imgCreator.compute1(new FinalDimensions(3, 3, 3));
        final ImgPlus<BitType> imgPlus = new ImgPlus<>(img);
        Octant<BitType> octant = new Octant<>(imgPlus);
        final Cursor<BitType> cursor = imgPlus.localizingCursor();
        final long[] location = new long[3];

        // fill test interval
        while (cursor.hasNext()) {
            cursor.next();
            cursor.localize(location);
            if (location[0] < 2 && location[1] < 2 && location[2] < 2) {
                cursor.get().setOne();
            }
        }

        octant.setNeighborhood(1, 1, 1);
        assertEquals("All neighbours should be foreground", 8, octant.getNeighborCount());

        octant.setNeighborhood(2, 2, 2);
        assertEquals("Wrong number of foreground neighbors", 1, octant.getNeighborCount());
    }
}