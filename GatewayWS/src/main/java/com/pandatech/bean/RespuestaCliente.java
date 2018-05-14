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
public class RespuestaCliente {

    private String autenticacion;
    private String firma;
    private String factura;
    private String comprobanteXml;

    public RespuestaCliente() {
    }

    
    public String getAutenticacion() {
        return autenticacion;
    }

    public void setAutenticacion(String autenticacion) {
        this.autenticacion = autenticacion;
    }

    public String getFirma() {
        return firma;
    }

    public void setFirma(String firma) {
        this.firma = firma;
    }

    public String getFactura() {
        return factura;
    }

    public void setFactura(String factura) {
        this.factura = factura;
    }

    public String getComprobanteXml() {
        return comprobanteXml;
    }

    public void setComprobanteXml(String comprobanteXml) {
        this.comprobanteXml = comprobanteXml;
    }
    
    
}
