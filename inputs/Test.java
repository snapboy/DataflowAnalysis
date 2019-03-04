public class Test {
    public static void main(String[] args) {
        test1(10, 11, 25, 33, 43, 26);
    }

    private static void test1(int a, int b, int c, int d, int e, int f) {
        a = c + 11;
        b = f * e;
        while (a < b)
        {
            a = b * c;
            d = b;
            if (d > c)
                b = b + 1;
            else
                e = d * c;
            f = b * c;
        }
    }

    private static void test(int a, int b, int c, int d, int i) {
        while (i < 100) {
            a = 210;
            if (c > 20) {
                a = 100 * 2;
                if (d < 100) {
                    d = 101;
                } else {
                    c = 100 * 120;
                }
                b = 300 + 210;
            } else {
                b = 21 + 210;
                c = 212 * 12;
                d = 200 * 11;
            }
            int y = a + b;
            int z = c + d;
            i = i + 1;
        }
    }

}