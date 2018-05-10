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
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.json.JsonObject;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
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
@WebService(serviceName = "Facturacion")
public class Facturacion {

    private static final String IDP_URI = "https://idp.comprobanteselectronicos.go.cr/auth/realms/rut-stag/protocol/openid-connect";
    private static final String IDP_CLIENT_ID = "api-stag";
    private static final String URI = "https://api.comprobanteselectronicos.go.cr/recepcion-sandbox/v1/";
    private String accessToken;
    private String refreshToken;
    private static String XML = "";
    private static String XML_firmado = "";
    private String xmlFirmado;
    private String extractoClaveXml;
    private String xmlBase64;
    Recepcion recepcion = new Recepcion();
    String archivoxml = null;

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "Facturacion")
    public String facturacion(@WebParam(name = "usuario") String usuario, @WebParam(name = "password") String password, @WebParam(name = "rutaCertificadop12") String rutaCertificadop12, @WebParam(name = "pin") String pin, @WebParam(name = "rutaXml") String rutaXml, @WebParam(name = "rutaGuardado") String rutaGuardado, @WebParam(name = "tipoIdEmisor") String tipoIdEmisor, @WebParam(name = "numeroIdEmisor") String numeroIdEmisor, @WebParam(name = "tipoIdReceptor") String tipoIdReceptor, @WebParam(name = "numeroIdReceptor") String numIdReceptor) throws InterruptedException {
        String res = "";
        Funciones proceso = new Funciones();
        res = "Usuario: " + proceso.autenticacion(usuario, password) + "\n\n";
        
        proceso.desconexion();
        return res;
    }

    /*
        res = proceso.firmaXml(rutaCertificadop12, pin, rutaXml, rutaGuardado) + "\n\n";
        proceso.creacionObjetoJson(tipoIdEmisor, numeroIdEmisor, tipoIdReceptor, numIdReceptor);
        res = "Respuesta al envío de la factura a hacienda: " + proceso.enviarDocumento() + "\n\n";
        Thread.sleep(3000);
        res = "Comprobante Hacienda: " + proceso.validacionEstado() + "\n";
    */
    
    
}
