/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pandatech.gatewayws;

import com.google.gson.Gson;
import com.pandatech.bean.IdentificacionEmisor;
import com.pandatech.bean.IdentificacionReceptor;
import com.pandatech.bean.Recepcion;
import com.pandatech.bean.Validacion;
import com.pandatech.servlet.Conversion;
import com.pandatech.servlet.Logica;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

/**
 *
 * @author Emmanuel Guzman
 */
@WebServlet(name = "Funciones", urlPatterns = {"/Funciones"})
public class Funciones extends HttpServlet {

    private static final String IDP_URI = "https://idp.comprobanteselectronicos.go.cr/auth/realms/rut-stag/protocol/openid-connect";
    private static final String IDP_CLIENT_ID = "api-stag";
    private static final String URI = "https://api.comprobanteselectronicos.go.cr/recepcion-sandbox/v1/";
    private String accessToken;
    private String refreshToken;
    private static String XML = "";
    private static String XML_firmado = "";
    private String xmlFirmado;
    private String extractoClaveXml;
    private String extractoTipoIdEmisor;
    private String extractoIdEmisor;
    private String xmlBase64;
    Recepcion recepcion = new Recepcion();
    String archivoxml = null;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Funciones</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Funciones at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);

    }

    //@WebMethod(operationName = "autenticacion")
    //public String autenticacion(@WebParam(name = "usuario") String usuario, @WebParam(name = "password") String password) {
    public String autenticacion(String usuario, String password) {
        String res;
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(IDP_URI + "/token");
            Form form = new Form();
            form.param("grant_type", "password")
                    .param("username", usuario)
                    .param("password", password)
                    .param("client_id", IDP_CLIENT_ID);
            Response respuesta = target.request().post(Entity.form(form));

            //La respuesta debe ser un código 200. Debe considerarse el caso en que retorne un valor diferente
            // Indicador que se ha enviado un atributo incorrecto
            JsonObject responseJson = respuesta.readEntity(JsonObject.class);

            // De la respuesta procedemos a leer el access y refresh token
            accessToken = responseJson.getString("access_token");
            refreshToken = responseJson.getString("refresh_token");
            if (accessToken.length() > 0 && refreshToken.length() > 0) {
                res = "Autenticado";
            } else {
                res = "Error en la autenticación, verifique su usuario y contraseña";
            }
        } catch (Exception e) {
            res = "Error en la autenticación, verifique su usuario y contraseña";
        }
        return res;
    }

    //@WebMethod(operationName = "firmaXml")
    //public String firmaXml(@WebParam(name = "rutaCertificadop12") String rutaCertificadop12, @WebParam(name = "pin") String pin, @WebParam(name = "rutaXml") String rutaXml, @WebParam(name = "rutaGuardado") String rutaGuardado) {
    public String firmaXml(String rutaCertificadop12, String pin, String rutaXml, String rutaGuardado) {

        ClassLoader classLoader = getClass().getClassLoader();
        String res;
        Process cat;
        String content = "";
        String rutaJarFirma = classLoader.getResource("firmar-xades.jar").getPath();

        String[] rutaFirmador = rutaJarFirma.split("");
        String[] rutaSave = rutaGuardado.split("");
        String rutaCorrecta = "";

        for (int i = 0; i < rutaFirmador.length; i++) {
            if (i > 0) {
                rutaCorrecta += rutaFirmador[i];
            }
        }
        
        String rutaFirma = rutaCorrecta.replace("%20", " ");

        
        
        try {
            cat = Runtime.getRuntime().exec("java -jar " + '"' + rutaFirma + '"' + " " + '"' + rutaCertificadop12 + '"' + " " + pin + " " + '"' + rutaXml + '"' + " " + '"' + rutaGuardado + '"');

            Thread.sleep(3000);

            //Lee el xml firmado en la ruta indicada
            content = readFile(rutaGuardado, StandardCharsets.UTF_8);

            xmlFirmado = content.toString();
            //se debe encontrar el numero de clave en el xml
            extractoClaveXml = xmlFirmado.substring(xmlFirmado.indexOf("<Clave>") + 7, xmlFirmado.indexOf("</Clave>"));
            extractoTipoIdEmisor = xmlFirmado.substring(xmlFirmado.indexOf("<Tipo>") + 6, xmlFirmado.indexOf("</Tipo>"));
            extractoIdEmisor = xmlFirmado.substring(xmlFirmado.indexOf("<Numero>") + 8, xmlFirmado.indexOf("</Numero>"));
            //System.out.println("----------------------------------------------------------------------------------------------------------------------------- ");
            //System.out.println("--------------------------------------VALIDACION CANTIDAD CARACTERES EN CLAVE ----------------------------------------------- ");
            //valida la clave sustraid cuente con los 50 caracteres
            //System.out.println(extractoIdEmisor + " " + extractoTipoIdEmisor);
            if (extractoClaveXml.length() < 50 || extractoClaveXml.length() > 50) {
                res = "La cantidad de caracteres en la clave deben ser 50, favor volver a validarla";
            } else {
                //System.out.println("Correcto " + extractoClaveXml.length());
                //se convierte el resultado del xml firmado en base 64
                Conversion codificar = new Conversion();
                xmlBase64 = codificar.encode(content);
                res = "Xml firmado exitosamente";
            }
        } catch (Exception e) {
            //System.out.println(e.toString());
            res = e.toString();
        }
        //Realiza el proceso en tiempo de ejecucion java -jar sobre el compilado jar para realizar la firma del archivo xml
        //Se utiliza 4 parametros para ejecutar el jar de firmado

        return res;
    }

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    //@WebMethod(operationName = "creacionObjetoJson")
    //public void creacionObjetoJson(@WebParam(name = "tipoIdReceptor") String tipoIdReceptor, @WebParam(name = "numeroIdReceptor") String numIdReceptor) {
    public void creacionObjetoJson(String tipoIdReceptor, String numIdReceptor) {

        //Se creó un objeto recepcion global
        //recepcion.setClave("506" + "010118" + "003101684401" + "0000000000000000013" + "1" + "999999999");
        recepcion.setClave(extractoClaveXml);

        //System.out.println(recepcion.getClave());
        recepcion.setFecha();

        IdentificacionEmisor emisor = new IdentificacionEmisor();
        emisor.setTipoIdentificacion(extractoTipoIdEmisor);
        emisor.setNumeroIdentificacion(extractoIdEmisor);

        recepcion.setIdentificacionEmisor(emisor);

        //El identificador del receptor es un valor opcional
        if (tipoIdReceptor != "0" && numIdReceptor != "0") {
            IdentificacionReceptor receptor = new IdentificacionReceptor();
            receptor.setTipoIdentificacion(tipoIdReceptor);
            receptor.setNumeroIdentificacion(numIdReceptor);
            recepcion.setIdentificacionReceptor(receptor);
        }

        recepcion.setComprobanteXml(xmlBase64);
    }

    //@WebMethod(operationName = "enviarDocumento")
    public String enviarDocumento() {
        String res = "";
        try {
            Client client = ClientBuilder.newClient();
            WebTarget target = client.target(URI + "recepcion");
            Invocation.Builder solicitud = target.request();
            solicitud.header("Authorization", "Bearer " + accessToken);

            Gson gson = new Gson();
            String jsonString = gson.toJson(recepcion);
            Response post = solicitud.post(Entity.json(jsonString));

            //Response post = solicitud.post(Entity.json(recepcion));
            //System.out.println(post.getStatus());
            switch (post.getStatus()) {
                case 202:
                    // Éste código de retorno se da por recibido a la plataforma el documento. Posteriormente
                    // debe validarse su estado de aceptación o rechazo. Es importante hacer notar que se
                    // regresa un header "Location" que corresponde a un URL. donde se puede validar el
                    // estado del documento, por ejemplo:
                    // https://api.comprobanteselectronicos.go.cr/recepcion-sandbox/v1/recepcion/50601011600310112345600100010100000000011999999999/
                    res = "Factura recibida de forma Satisfactoria!";
                    break;
                case 400:
                    // Se da si se detecta un error en las validaciones, por ejemplo: si intento enviar más de una
                    // vez un documento. El encabezado "X-Error-Cause" indica la causa del problema.
                    res = post.getHeaderString("X-Error-Cause");
                    /*
                    System.out.println(post.getHeaderString("X-Ratelimit-Limit"));
                    System.out.println(post.getHeaderString("X-Ratelimit-Remaining"));
                    System.out.println(post.getHeaderString("X-Ratelimit-Reset"));
                    LOG.log(Level.SEVERE, xErrorCause);
                     */
                    break;
            }
        } catch (Exception e) {
            res = e.toString();
        }
        return res;
    }

    //@WebMethod(operationName = "comprobanteXml")
    //public String comprobanteXml(@WebParam(name = "clave") String clave) {
    public String comprobanteXml(String clave) {
        String respuesta = "";
        //System.out.println(clave);
        Client client = ClientBuilder.newClient();
        // En éste caso, clave corresponde a la clave del documento a validar
        WebTarget target = client.target(URI + "recepcion/" + clave);
        Invocation.Builder request = target.request();

        // Se debe brindar un header "Authorization" con el valor del access token obtenido anteriormente.
        request.header("Authorization", "Bearer " + accessToken);

        // Se envía un GET. para tomar el estado
        Response res = request.get();

        //System.out.println(res.getStatus());
        switch (res.getStatus()) {
            case 200:
                // Acá se debe procesar la respuesta para determinar si el atributo "ind-estado"
                // del JSON. de respuesta da por aceptado o rechazado el documento. Si no está
                // en ese estado se debe reintentar posteriormente.
                /*
                System.out.println(res.getStatusInfo());
                System.out.println(res);
                 */
                String output = res.readEntity(String.class).replace("ind-estado", "ind_estado").replace("respuesta-xml", "respuesta_xml");

                //Genera el xml en consola de la respuesta de hacienda
                //System.out.println(output);
                try {
                    final Gson gson = new Gson();
                    final Validacion json = gson.fromJson(output, Validacion.class);

                    if (json.getInd_estado().equals("error")) {
                        respuesta = "Comprobante no generado, intente mas tarde";
                    } else {
                        Conversion decodificar = new Conversion();
                        archivoxml = decodificar.decode(json.getRespuestaXml());
                        //System.out.println(archivoxml);
                        respuesta = archivoxml;
                        //System.out.println(respuesta);
                    }

                } catch (Exception e) {
                    respuesta = e.toString();
                }

                break;
            case 404:
                // Se presenta si no se localiza la clave brindada
                //LOG.log(Level.SEVERE, "La clave no esta registrada");
                respuesta = "Error, la clave no esta registrada";
                break;
            case 400:
                respuesta = res.getHeaderString("X-Error-Cause");
                break;

        }
        return respuesta;
    }

    //@WebMethod(operationName = "desconexion")
    public String desconexion() {
        String respuesta = "";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(IDP_URI + "/logout");

        // Los tokens son los obtenidos durante la fase de login inicial.
        Response response = target.request().header("Authorization", "Bearer " + accessToken).post(Entity.form(new Form("refresh_token", refreshToken)));
        respuesta = "desconectado";
        return respuesta;
    }

    public String guardarXml(String xml) {
        String respuesta = "";
        try {
            String ruta = "C:/Users/Emmanuel Guzman/Desktop/archivos/sinFirmar/sinFirma.xml";
            File archivo = new File(ruta);
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
            bw.write(xml);
            bw.close();
            respuesta = "Guardado";
        } catch (Exception e) {
            respuesta = e.toString();
        }
        return respuesta;
    }

    public String getExtractoClaveXml() {
        return extractoClaveXml;
    }

    public void setExtractoClaveXml(String extractoClaveXml) {
        this.extractoClaveXml = extractoClaveXml;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
