public class initself {
    public void test() {
        String a = "outer";
        {
            String a = a;;
        }
    }
}
