import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
@SuppressWarnings({ "rawtypes", "unchecked" })
/**
 * @author Poke Shedman
 *
 */
public class GVBCLASSLOADER2 extends ClassLoader {

    public ClassLoader classLoader = null;
    public Constructor<?> constructor = null;
    public Class<?> loadedMyClass = null;
    public Object myClassObject = null;

      public Method obtainClassMethod(String classBinName, String methodName, Class aarg[]){

        Method method = null;

        try {

            // Create a new JavaClassLoader
            classLoader = this.getClass().getClassLoader();

            // Load the target class using its binary name
            loadedMyClass = classLoader.loadClass(classBinName);
            //System.out.println("Loaded class name: " + loadedMyClass.getName());

            // Create a new instance from the loaded class
            constructor = loadedMyClass.getConstructor();
            myClassObject = this.constructor.newInstance();

            // Create a new instance of method
            //System.out.println("methodName:" + methodName);
            method = loadedMyClass.getMethod(methodName,aarg);
           //System.out.println("Invoked method name: " + method.getName());

         } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //return myClassObject;
        return method;
    }

    public String executeClassMethod(Method method, String stuff){
        String result = " ";

        try {
            Object methodObject = method.invoke(myClassObject, stuff);
            //System.out.println("Object aaaa:" + aaaa);
            result = methodObject.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }    
}