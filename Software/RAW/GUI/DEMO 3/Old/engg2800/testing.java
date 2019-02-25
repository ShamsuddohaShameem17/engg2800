package engg2800;

public class testing {
    static String test_switch(String s ){
        switch (s){
            case "read":
                return "read";
            case "write":
                return "write";
            default:
                return "nope";
        }
    }

    public static void main(String[] args) {
        String s = "writ";
        System.out.println(test_switch(s));


    }
}
