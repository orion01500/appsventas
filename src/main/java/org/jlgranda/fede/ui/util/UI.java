/*
 * Copyright (C) 2016 jlgranda
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package org.jlgranda.fede.ui.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.model.SelectItem;
import org.jlgranda.fede.model.document.DocumentType;
import org.jlgranda.fede.model.document.EmissionType;
import org.jlgranda.fede.model.management.Organization;
import org.jlgranda.fede.model.sales.ProductType;
import org.jpapi.util.I18nUtil;

/**
 * Utilidades para la construcción de vistas
 *
 * @author jlgranda
 */
@ManagedBean(name = "ui")
@RequestScoped
public class UI {

    @PostConstruct
    public void init() {
    }
    
    public Organization.Type[] getOrganizationTypes() {
        return Organization.Type.values();
    }
    
    public DocumentType[] getDocumentTypes() {
        return DocumentType.values();
    }
    
    public ProductType[] getProductTypes() {
        return ProductType.values();
    }
    
    
    public EmissionType[] getEmissionTypes() {
        return EmissionType.values();
    }
    
    public List<SelectItem> getDocumentTypesAsSelectItem() {
        List<SelectItem> items = new ArrayList<>();
        SelectItem item = null;
        for (DocumentType t : getDocumentTypes()) {
            item = new SelectItem(t, I18nUtil.getMessages(t.name()));
            items.add(item);
        }
        return items;
    }
    
    public List<SelectItem> getProductTypesAsSelectItem() {
        List<SelectItem> items = new ArrayList<>();
        SelectItem item = null;
        for (ProductType t : getProductTypes()) {
            item = new SelectItem(t, I18nUtil.getMessages(t.name()));
            items.add(item);
        }
        return items;
    }
    public List<SelectItem> getEmisionTypesAsSelectItem() {
        List<SelectItem> items = new ArrayList<>();
        SelectItem item = null;
        for (EmissionType t : getEmissionTypes()) {
            item = new SelectItem(t, I18nUtil.getMessages(t.name()));
            items.add(item);
        }
        return items;
    }
    
    public List<SelectItem> getOrganizationTypesAsSelectItem() {
        List<SelectItem> items = new ArrayList<>();
        SelectItem item = null;
        for (Organization.Type t : getOrganizationTypes()) {
            item = new SelectItem(t, I18nUtil.getMessages(t.name()));
            items.add(item);
        }
        return items;
    }
    
     public static SelectItem[] getSelectItems(List<?> entities, boolean selectOne) {
        int size = selectOne ? entities.size() + 1 : entities.size();
        SelectItem[] items = new SelectItem[size];
        int i = 0;
        if (selectOne) {
            items[0] = new SelectItem("", "---");
            i++;
        }
        for (Object x : entities) {
            items[i++] = new SelectItem(x, x.toString());
        }
        return items;
    }
    

    public List<SelectItem> getValuesAsSelectItem(List<Object> values) {
        List<SelectItem> items = new ArrayList<>();
        SelectItem item = null;
        item = new SelectItem(null, I18nUtil.getMessages("common.choice"));
        items.add(item);
        for (Object o : values) {
            item = new SelectItem(cleanValue(o), cleanValue(o).toString());
            items.add(item);
        }

        return items;
    }
    
    /**
     * Calcula el tamaño de contenedor para el tamaño de elementos 
     * identificado por size
     * @param size
     * @return el contenedor adecuado para size
     */
    public int calculeContainer(long size){
        return (int) (100 / size);
    }

    private Object cleanValue(Object value) {
        
        if (value == null) {
            return null;
        }
        if (!(value instanceof String)){
            return value;
        }
        
        String cleaned = value.toString();

        if (cleaned.contains("*")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        return cleaned;
    }
    
    /**
     * Imprime emoticons para ocultar la cantidad factor
     * @param total
     * @param gap
     * @return 
     */
    public String calculeEmoticon(BigDecimal total, int gap){
        String emoticon = "<i class=\"fa  fa-flag-o\"></i>";
        int half_gap = gap / 2;
        if (total.compareTo(BigDecimal.valueOf(gap)) > 0){
            int factor;
            factor = total.intValue() / gap;
            emoticon = "";
            for (int i=0; i < factor; i++){
                emoticon = emoticon + "<i class=\"fa fa-flag\"></i>";
            }
            
            BigDecimal excedente = total.subtract(new BigDecimal(factor * gap));
            if (excedente.compareTo(BigDecimal.valueOf(half_gap)) > 0){
                emoticon = emoticon + "<i class=\"fa fa-flag-checkered\"></i>";
            } else {
                emoticon = emoticon + "<i class=\"fa fa-flag-o\"></i>";
            }
        } else if (total.compareTo(BigDecimal.valueOf(half_gap)) > 0){
            emoticon = "<i class=\"fa fa-flag-checkered\"></i>";
        } 
        return emoticon;
    }
    /**Verifica que se haya cumplido con el salto dado para notificación de cumplimiento de totales
     * @param total
     * @param gap
     * @return true si se ha completa la mitad y/o el total, falso en caso contrario
     */
    public static boolean isOver(BigDecimal total, int gap){
        boolean isOver = false;
        int half_gap = gap / 2;
        int factor;
        if (total.compareTo(BigDecimal.valueOf(gap)) > 0){
            factor = total.intValue() % gap;
            isOver = factor == 0;
        } else if (total.compareTo(BigDecimal.valueOf(half_gap)) > 0){
            factor = total.intValue() % half_gap;
            isOver = factor == 0;
        } 
        return isOver;
    }


    public static Integer calculePorcentaje(int pageWidth, int porcentaje) {
        
        double factor = (porcentaje / (double) 100);
        int valor = (int) (pageWidth * factor);
        System.out.println(">>> pageWidth: " + pageWidth + ", pocentaje:" + porcentaje + ", factor" + factor+ ", valor " + valor);
        return valor;
    }
    
    public static void main(String[] args) {
        System.out.println(new org.apache.commons.codec.digest.Crypt().crypt("f3d3"));
        
        UI.calculePorcentaje(297, 10);
        UI.calculePorcentaje(297, 60);
        UI.calculePorcentaje(297, 15);
        UI.calculePorcentaje(297, 15);
    }

}
