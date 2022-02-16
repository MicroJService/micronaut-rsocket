/**
 * @author CoderYellow
 */
public class Test {

    public static void main(String[] args) {
        Consumer<? super Sub> consumer = new Consumer<Parent>();

        consumer.consume(new SSSub());

        Object object = consumer.getObject();
    }
}
