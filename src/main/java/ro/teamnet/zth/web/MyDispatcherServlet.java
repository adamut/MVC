package ro.teamnet.zth.web;

import ro.teamnet.zth.api.annotations.MyController;
import ro.teamnet.zth.api.annotations.MyRequestMethod;
import ro.teamnet.zth.appl.controller.DepartmentController;
import ro.teamnet.zth.appl.controller.EmployeeController;
import ro.teamnet.zth.fmk.AnnotationScanUtils;
import ro.teamnet.zth.fmk.MethodAttributes;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by Cosmin.Adamut on 7/20/2017.
 */
public class MyDispatcherServlet extends HttpServlet {
   private Map<String, MethodAttributes> myMap = new HashMap<String, MethodAttributes>();

    public void init() {
        try {
            Iterable<Class> iterable = AnnotationScanUtils.getClasses("ro.teamnet.zth.appl.controller");
            for (Class it : iterable) {
                Method methods[] = it.getDeclaredMethods();
                for (Method currentMethod : methods) {
                    MethodAttributes methodAttributes = new MethodAttributes();
                    methodAttributes.setControllerClass(it.getClass().getName());
                    methodAttributes.setMethodName(currentMethod.getName());
                    methodAttributes.setMethodType(currentMethod.getReturnType().getTypeName());
                    String key;
                    MyController myController = (MyController) it.getDeclaredAnnotation(MyController.class);
                    key = myController.urlPath() +
                            currentMethod.getAnnotation(MyRequestMethod.class).urlPath() + "_" + currentMethod.getAnnotation(MyRequestMethod.class).methodType()
                    ;
                    myMap.put(key, methodAttributes);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //TODO de completat cu dispatchREply
        // resp.getWriter().write("dispatch");
        String methodType = "GET";
        dispatchReply(req, resp, methodType);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String methodType = "POST";
        dispatchReply(req, resp, methodType);
    }

    public void dispatchReply(HttpServletRequest request, HttpServletResponse response, String methodType) {
        try {
            Object resultToDisplay = dispatch(request, methodType);
            reply(response, resultToDisplay);
        } catch (Exception e) {
            sendExceptionError(e);
        }

    }

    private Object dispatch(HttpServletRequest request, String methodType) {
        String URI = request.getRequestURI();
        String resp = "";
        String splittedURI[] = URI.split("/");

        List<String> listSplitted = new ArrayList<String>();
        int i = 0;
        while (i < splittedURI.length)
            listSplitted.add(splittedURI[i++]);

        if (listSplitted.contains("employees")) {
            int mvcPoz = listSplitted.indexOf("mvc");
            int employeesPoz = listSplitted.indexOf("employees");
            if (mvcPoz + 1 == employeesPoz && employeesPoz + 1 == listSplitted.size()) {
                EmployeeController ec = new EmployeeController();
                resp = ec.getAllEmployees();
            }
        }

        if (listSplitted.contains("departments")) {
            int mvcPoz = listSplitted.indexOf("mvc");
            int departmentsPoz = listSplitted.indexOf("departments");
            if (mvcPoz + 1 == departmentsPoz && departmentsPoz + 1 == listSplitted.size()) {
                DepartmentController departmentController = new DepartmentController();
                resp = departmentController.getAllDepartments();
            }
        }

        return resp;
    }

    private void reply(HttpServletResponse response, Object responseObject) {
        try {
            response.getWriter().write((String) responseObject);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendExceptionError(Exception e) {
        System.out.println("Error!!!!   " + e.toString());
    }
}
