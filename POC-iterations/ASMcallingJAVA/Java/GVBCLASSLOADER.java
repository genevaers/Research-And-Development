import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
/**
 * @author Poke Shedman
 *
 */
public class GVBCLASSLOADER extends ClassLoader {
    public String invokeClassMethod(String classBinName, String methodName, String stuff){
        String bbbb = " ";
        try {
            // Create a new JavaClassLoader
            ClassLoader classLoader = this.getClass().getClassLoader();
            // Load the target class using its binary name
            Class<?> loadedMyClass = classLoader.loadClass(classBinName);
            //System.out.println("Loaded class name: " + loadedMyClass.getName());

            // Create a new instance from the loaded class
            Constructor<?> constructor = loadedMyClass.getConstructor();
            Object myClassObject = constructor.newInstance();

            // Create argument list for method, i.e. on string parameter
            Class aarg[] = new Class[1];
            aarg[0] = String.class;

            // Getting the target method from the loaded class and invoke it using its name
            Method method = loadedMyClass.getMethod(methodName,aarg);
            //System.out.println("Invoked method name: " + method.getName());

            //method.invoke(myClassObject);
            Object aaaa = method.invoke(myClassObject, stuff);
            //System.out.println("Object aaaa:" + aaaa);

            bbbb = aaaa.toString();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bbbb;
    }
}