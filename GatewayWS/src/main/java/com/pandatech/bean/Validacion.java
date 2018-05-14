/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.pandatech.bean;

/**
 *
 * @author Emmanuel Guzman
 */
public class Validacion {
    private String clave;
    private String fecha;
    private String ind_estado;
    private String respuesta_xml;

    public Validacion() {
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public void setInd_estado(String ind_estado) {
        this.ind_estado = ind_estado;
    }

    public void setRespuesta_xml(String respuesta_xml) {
        this.respuesta_xml = respuesta_xml;
    }
    
    
    
    public String getClave() {
        return clave;
    }

    public String getFecha() {
        return fecha;
    }

    public String getInd_estado() {
        return ind_estado;
    }

    public String getRespuestaXml() {
        return respuesta_xml;
    }
    
    
    
    
    
    
}
