/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlgranda.fede.ui.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import net.tecnopro.document.ejb.TareaService;
import net.tecnopro.document.model.Tarea;
import net.tecnopro.document.model.Tarea_;
import org.jlgranda.fede.model.sales.Invoice;
import org.jpapi.model.BussinesEntity;
import org.jpapi.model.BussinesEntityType;
import org.jpapi.model.profile.Subject;
import org.jpapi.util.QueryData;
import org.jpapi.util.QuerySortOrder;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jorge
 */
public class LazyTareaDataModel extends LazyDataModel<Tarea> implements Serializable {

    private static final int MAX_RESULTS = 5;
    Logger logger = LoggerFactory.getLogger(LazyTareaDataModel.class);
    private TareaService tareaService;
    private List<Tarea> resultList;
    private int firstResult = 0;

    private BussinesEntityType type;

    private Subject author;

    private Subject owner;
    /**
     * Lista de etiquetas para filtrar facturas
     */
    private String tags;

    /**
     * Inicio del rango de fecha
     */
    private Date start;

    /**
     * Fin del rango de fecha
     */
    private Date end;

    private String typeName;

    private BussinesEntity[] selectedBussinesEntities;

    private BussinesEntity selectedBussinesEntity; //Filtro de cuenta schema

    private String filterValue;

    public LazyTareaDataModel(TareaService bussinesEntityService) {
        setPageSize(MAX_RESULTS);
        resultList = new ArrayList<>();
        this.tareaService = bussinesEntityService;
    }

    @PostConstruct
    public void init() {
    }

    public TareaService getTareaService() {
        return tareaService;
    }

    public void setTareaService(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    public List<Tarea> getResultList() {
        logger.info("load BussinesEntitys");

        if (resultList.isEmpty()/* && getSelectedBussinesEntity() != null*/) {
            resultList = tareaService.find(this.getPageSize(), this.getFirstResult());
        }
        return resultList;
    }

    public int getFirstResult() {
        return firstResult;
    }

    public void setResultList(List<Tarea> resultList) {
        this.resultList = resultList;
    }

    public int getNextFirstResult() {
        return firstResult + this.getPageSize();
    }

    public int getPreviousFirstResult() {
        return this.getPageSize() >= firstResult ? 0 : firstResult - this.getPageSize();
    }

    public void setFirstResult(int firstResult) {
        logger.info("set first result + firstResult");
        this.firstResult = firstResult;
        this.resultList = null;;
    }

    public boolean isPreviousExists() {
        return firstResult > 0;
    }

    public boolean isNextExists() {
        return tareaService.count() > this.getPageSize() + firstResult;
    }

    @Override
    public Tarea getRowData(String rowKey) {
        return tareaService.find(Long.valueOf(rowKey));
    }

    @Override
    public Object getRowKey(Tarea entity) {
        return entity.getName();
    }

    public List<Tarea> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {

        int end = first + pageSize;

        QuerySortOrder order = QuerySortOrder.DESC;
        if (sortOrder == SortOrder.ASCENDING) {
            order = QuerySortOrder.ASC;
        }
        Map<String, Object> _filters = new HashMap<>();
        Map<String, Date> range = new HashMap<>();
        range.put("start", getStart());
        range.put("end", getEnd());
        //_filters.put(BussinesEntity_.type.getName(), getType()); //Filtro por defecto
        _filters.put(Tarea_.author.getName(), getAuthor()); //Filtro por defecto
        _filters.put(Tarea_.createdOn.getName(), range); //Filtro de fecha inicial
        if (getTags() != null && !getTags().isEmpty()) {
            _filters.put("tag", getTags()); //Filtro de etiquetas
        }

        if (getFilterValue() != null && !getFilterValue().isEmpty()) {
            _filters.put("keyword", getFilterValue()); //Filtro general
        }

        _filters.putAll(filters);

        if (sortField == null) {
            sortField = Tarea_.createdOn.getName();
        }

        QueryData<Tarea> qData = tareaService.find(first, end, sortField, order, _filters);
        this.setRowCount(qData.getTotalResultCount().intValue());
        return qData.getResult();
    }

    public BussinesEntityType getType() {
        return type;
    }

    public void setType(BussinesEntityType type) {
        this.type = type;
    }

    public Subject getAuthor() {
        return author;
    }

    public void setAuthor(Subject author) {
        this.author = author;
    }

    public Subject getOwner() {
        return owner;
    }

    public void setOwner(Subject owner) {
        this.owner = owner;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public BussinesEntity[] getSelectedBussinesEntities() {
        return selectedBussinesEntities;
    }

    public void setSelectedBussinesEntities(BussinesEntity[] selectedBussinesEntities) {
        this.selectedBussinesEntities = selectedBussinesEntities;
    }

    public BussinesEntity getSelectedBussinesEntity() {
        return selectedBussinesEntity;
    }

    public void setSelectedBussinesEntity(BussinesEntity selectedBussinesEntity) {
        this.selectedBussinesEntity = selectedBussinesEntity;
    }

    public String getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(String filterValue) {
        this.filterValue = filterValue;
    }

}