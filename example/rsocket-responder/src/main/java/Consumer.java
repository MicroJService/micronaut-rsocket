/**
 * @author CoderYellow
 */
public class Consumer<T> {

    private T object;

    public void consume(T object) {
        this.object = object;

    }

    public T getObject() {
        return object;
    }
}
