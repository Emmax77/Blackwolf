/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pandatech.servlet;

import com.google.gson.Gson;
import com.pandatech.bean.IdentificacionEmisor;
import com.pandatech.bean.IdentificacionReceptor;
import com.pandatech.bean.Recepcion;
import com.pandatech.bean.Validacion;
import java.io.IOException;
import java.io.PrintWriter;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.jws.WebMethod;

// Clases para la creacion y manejo de XML
/**
 *
 * @author Emmanuel GR
 */
@WebServlet(name = "Logica", urlPatterns = {"/Logica"})
@MultipartConfig(fileSizeThreshold = 6291456, // 6 MB
        maxFileSize = 10485760L, // 10 MB
        maxRequestSize = 20971520L // 20 MB
)

//@WebService
public class Logica extends HttpServlet {

    private static final String IDP_URI = "https://idp.comprobanteselectronicos.go.cr/auth/realms/rut-stag/protocol/openid-connect";
    private static final String IDP_CLIENT_ID = "api-stag";
    private static String usu = "cpj-3-101-684401@stag.comprobanteselectronicos.go.cr";
    private static String pass = "X=!:&OvjqB#C_)XO@#B]";

    private static final String UPLOAD_DIR = "uploads";
    private static final String URI = "https://api.comprobanteselectronicos.go.cr/recepcion-sandbox/v1/";

    private String accessToken;
    private String refreshToken;

    private static final String JAR_DIR = " C:/Users/PCPTUser/Desktop/prueba/firmar-xades.jar ";
    private static final String LLAVE_DIR = " C:/Users/PCPTUser/Desktop/prueba/llavecriptografica_310168440106.p12 ";
    private static final String LLAVE_CLAVE_DIR = " 8888 ";
    private static String XML = "";
    private static String XML_firmado = "";

    private String xmlFirmado;
    private String extractoClaveXml;
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
            out.println("<title>Servlet Autenticacion</title>");
            out.println("</head>");
            out.println("<body>");
            //out.println("<h1>Servlet Autenticacion at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            //out.println("<script>alert('"+ "AccessToken: " + accessToken +"')</script>");
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
    @WebMethod
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
        System.out.println(autenticacion(request.getParameter("usuario"), request.getParameter("password")));
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

        response.setContentType("text/html;charset=UTF-8");

        //System.out.println(autenticacion(request.getParameter("usuario"), request.getParameter("password")));
        //creacionObjetoJson();
        //String llego = enviarDocumento();
        //validacionEstado();
        //System.out.println(desconexion());

        /*
        Gson gson = new Gson();
        String jsonString = gson.toJson(recepcion);
        System.out.println(jsonString);
         */
    }//cierre dopost
    //Ejecuta proceso para leer archivo xml

    public String xml(String rutaCertificadop12, String pin, String rutaXml, String rutaGuardado) {
        String res;
        Process cat;
        String content = "";

        URL ruta = Logica.class.getProtectionDomain().getCodeSource().getLocation();
        //System.out.println(ruta.toString().replace("com/pandatech/servlet/Logica.class", "archivos/firmar-xades.jar"));
        String rutaJarFirma = ruta.toString().replace("com/pandatech/servlet/Logica.class", "archivos/firmar-xades.jar");
        try {
            cat = Runtime.getRuntime().exec("java -jar" + rutaJarFirma + " " + rutaCertificadop12 + " " + pin + " " + rutaXml + " " + rutaGuardado);

            // Process cat = Runtime.getRuntime().exec("java -jar"  + JAR_DIR + LLAVE_DIR + LLAVE_CLAVE_DIR + XML + XML_firmado);  
            // Process cat = Runtime.getRuntime().exec("java -jar " + JAR_DIR + LLAVE_DIR + LLAVE_CLAVE_DIR + XML + " C:/Users/PCPTUser/Desktop/prueba/prueba1.xml ");
            //Lee el xml firmado en la ruta indicada
            content = readFile(rutaGuardado, StandardCharsets.UTF_8);
            System.out.println("------------------------------------------------------------------------------------------------ ");
            System.out.println("--------------------------------------XML FIRMADO----------------------------------------------- ");

            System.out.println(content);
            xmlFirmado = content.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        //Realiza el proceso en tiempo de ejecucion java -jar sobre el compilado jar para realizar la firma del archivo xml
        //Se utiliza 4 parametros para ejecutar el jar de firmado

        //se debe encontrar el numero de clave en el xml
        extractoClaveXml = xmlFirmado.substring(xmlFirmado.indexOf("<Clave>") + 7, xmlFirmado.indexOf("</Clave>"));

        //System.out.println("----------------------------------------------------------------------------------------------------------------------------- ");
        //System.out.println("--------------------------------------VALIDACION CANTIDAD CARACTERES EN CLAVE ----------------------------------------------- ");
        //valida la clave sustraid cuente con los 50 caracteres
        if (extractoClaveXml.length() < 50 || extractoClaveXml.length() > 50) {
            res = "La cantidad de caracteres en la clave deben ser 50, favor volver a validarla";
        } else {
            System.out.println("Correcto " + extractoClaveXml.length());
            //se convierte el resultado del xml firmado en base 64
            Conversion codificar = new Conversion();
            xmlBase64 = codificar.encode(content);
            res = xmlBase64;
        }

        return res;
    }

    public static String readFile(String path, Charset encoding)
            throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public String autenticacion(String usuario, String password) {
        String res;
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(IDP_URI + "/token");
        Form form = new Form();
        form.param("grant_type", "password")
                .param("username", usuario)
                .param("password", password)
                .param("client_id", IDP_CLIENT_ID);
        Response respuesta = target.request().post(Entity.form(form));
        System.out.println(respuesta);
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
        return res;
    }

    public void creacionObjetoJson(String tipoIdEmisor, String numeroIdEmisor, String tipoIdReceptor, String numIdReceptor) {
        //Se creó un objeto recepcion global
        //recepcion.setClave("506" + "010118" + "003101684401" + "0000000000000000013" + "1" + "999999999");
        recepcion.setClave(extractoClaveXml);

        //System.out.println(recepcion.getClave());
        recepcion.setFecha();

        IdentificacionEmisor emisor = new IdentificacionEmisor();
        emisor.setTipoIdentificacion(tipoIdEmisor);
        emisor.setNumeroIdentificacion(numeroIdEmisor);

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
            System.out.println(post.getStatus());

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

    public String validacionEstado() {
        String respuesta = "";
        Client client = ClientBuilder.newClient();
        // En éste caso, clave corresponde a la clave del documento a validar
        WebTarget target = client.target(URI + "recepcion/" + recepcion.getClave());
        Invocation.Builder request = target.request();

        // Se debe brindar un header "Authorization" con el valor del access token obtenido anteriormente.
        request.header("Authorization", "Bearer " + accessToken);

        // Se envía un GET. para tomar el estado
        Response res = request.get();

        System.out.println(res.getStatus());

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

                    Conversion decodificar = new Conversion();
                    archivoxml = decodificar.decode(json.getRespuestaXml());
                    //System.out.println(archivoxml);
                    respuesta = archivoxml;
                } catch (Exception e) {
                    respuesta = e.toString();
                }

                break;
            case 404:
                // Se presenta si no se localiza la clave brindada
                //LOG.log(Level.SEVERE, "La clave no esta registrada");
                respuesta = "La clave no esta registrada";
                break;
            case 400:
                respuesta = res.getHeaderString("X-Error-Cause");
                break;

        }
        return respuesta;
    }

    public String desconexion() {
        String respuesta = "";
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(IDP_URI + "/logout");

        // Los tokens son los obtenidos durante la fase de login inicial.
        Response response = target.request().header("Authorization", "Bearer " + accessToken).post(Entity.form(new Form("refresh_token", refreshToken)));
        respuesta = "desconectado";
        return respuesta;
    }

    public String comprobanteXml() {
        String respuesta = "";
        try {
            String ruta = "C://temp/PT-" + recepcion.getClave() + ".xml";
            File archivo = new File(ruta);
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivo));
            bw.write(archivoxml);
            bw.close();
            respuesta = "Comprobante Xml creado en la siguiente ruta: " + ruta;
        } catch (Exception e) {
            respuesta = e.toString();
        }
        return respuesta;
    }

    //envia correo electronico segun el correo emisor,la clave del emisor y su respectivo destinatario
    public String envioCorreo(String correoEmisor, String password, String correodestinatario) {
        String respuesta = "";
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.port", "587");
        props.setProperty("mail.smtp.user", correoEmisor);
        props.setProperty("mail.smtp.auth", "true");

        Session session = Session.getDefaultInstance(props, null);
        session.setDebug(true);

        //Construccion del correo
        try {
            BodyPart texto = new MimeBodyPart();
            texto.setText("Prueba de envío de xml");//texto dentro del correo
            //colocacion de adjunto
            BodyPart adjunto = new MimeBodyPart();
            adjunto.setDataHandler(new DataHandler(new FileDataSource("C://temp/PT-" + recepcion.getClave() + ".xml")));
            adjunto.setFileName(recepcion.getClave() + ".xml");//Nombre del archivo para que cliente lo lea antes de abrirlo
            //Juntar texto y adjunto
            MimeMultipart multiParte = new MimeMultipart();
            multiParte.addBodyPart(texto);
            multiParte.addBodyPart(adjunto);

            //Creación del cuerpo del mensaje
            MimeMessage message = new MimeMessage(session);

            // Se rellena el From
            message.setFrom(new InternetAddress(correoEmisor));

            // Se rellenan los destinatarios    
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(correodestinatario));

            // Se rellena el subject o asunto
            message.setSubject("Prueba de correo para xml");

            // Se mete el texto y la foto adjunta.
            message.setContent(multiParte);

            //Enviar correo
            Transport t = session.getTransport("smtp");
            t.connect(correoEmisor, password);
            t.sendMessage(message, message.getAllRecipients());
            t.close();
            respuesta = "Correo enviado";
        } catch (Exception e) {
            respuesta = e.toString();
        }
        return respuesta;
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
