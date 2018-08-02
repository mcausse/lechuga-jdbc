package udb2019;

import org.junit.Assert;

/**
 * <ul>
 * <li>negative integer - less than
 * <li>zero - equal to
 * <li>positive integer - greater than
 * </ul>
 *
 * @see Comparable#compareTo(Object)
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class ComparableUtils {

    public static <K extends Comparable<K>> K max(K a, K b) {
        if (lt(a, b)) {
            return b;
        } else {
            return a;
        }
    }

    public static <K extends Comparable<K>> K min(K a, K b) {
        if (gt(a, b)) {
            return b;
        } else {
            return a;
        }
    }

    public static boolean eq(Comparable a, Comparable b) {
        return a.compareTo(b) == 0;
    }

    public static boolean ne(Comparable a, Comparable b) {
        return a.compareTo(b) != 0;
    }

    public static boolean gt(Comparable a, Comparable b) {
        return a.compareTo(b) > 0;
    }

    public static boolean ge(Comparable a, Comparable b) {
        return a.compareTo(b) >= 0;
    }

    public static boolean lt(Comparable a, Comparable b) {
        return a.compareTo(b) < 0;
    }

    public static boolean le(Comparable a, Comparable b) {
        return a.compareTo(b) <= 0;
    }

    public static void main(String[] args) {

        Assert.assertFalse(eq(2, 1));
        Assert.assertTrue(eq(2, 2));
        Assert.assertTrue(ne(2, 1));
        Assert.assertFalse(ne(2, 2));

        Assert.assertTrue(gt(2, 1));
        Assert.assertFalse(gt(2, 2));
        Assert.assertFalse(gt(2, 3));

        Assert.assertTrue(ge(2, 1));
        Assert.assertTrue(ge(2, 2));
        Assert.assertFalse(ge(2, 3));

        Assert.assertFalse(lt(2, 1));
        Assert.assertFalse(lt(2, 2));
        Assert.assertTrue(lt(2, 3));

        Assert.assertFalse(le(2, 1));
        Assert.assertTrue(le(2, 2));
        Assert.assertTrue(le(2, 3));

    }
}
