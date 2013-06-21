package ch.cyberduck.core.lifecycle;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @version $Id:$
 */
public class LifecycleConfigurationTest {

    @Test
    public void testEquals() {
        assertEquals(new LifecycleConfiguration(), new LifecycleConfiguration());
        assertEquals(new LifecycleConfiguration(1, 1), new LifecycleConfiguration(1, 1));
        assertEquals(new LifecycleConfiguration(1, 2), new LifecycleConfiguration(1, 2));
        assertFalse(new LifecycleConfiguration(1, 2).equals(new LifecycleConfiguration(2, 1)));
    }
}
