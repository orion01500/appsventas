/*
 * Copyright (C) 2016 Jorge
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jlgranda.fede.controller.security;

import com.jlgranda.fede.SettingNames;
import com.jlgranda.fede.ejb.GroupService;
import com.jlgranda.fede.ejb.SubjectService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.apache.commons.collections.ListUtils;
import org.jlgranda.fede.controller.FedeController;
import org.jlgranda.fede.controller.SettingHome;
import org.jlgranda.fede.ui.model.LazyUserMemberGroupDataModel;
import org.jpapi.model.BussinesEntity;
import org.jpapi.model.profile.Subject;
import org.jpapi.util.I18nUtil;
import org.omnifaces.cdi.ViewScoped;
import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.PartitionManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.basic.BasicModel;
import org.picketlink.idm.model.basic.Group;
import org.picketlink.idm.model.basic.GroupMembership;
import org.picketlink.idm.model.basic.User;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author Jorge
 */
@Named(value = "securityGroupUserHome")
@ViewScoped
public class SecurityGroupUserHome extends FedeController implements Serializable {

    @Inject
    private PartitionManager partitionManager;
    @Inject
    SecurityGroupService securityGroupService;
    @Inject
    private SettingHome settingHome;
    IdentityManager identityManager = null;
    RelationshipManager relationshipManager = null;
    @Resource
    private UserTransaction userTransaction;
    @EJB
    SubjectService subjectService;
    @EJB
    private GroupService groupService;
    private List<BussinesEntity> selectedSubjects;
    private Group[] gruposSeleccionados;
    private List<Group> selectedGroups = new ArrayList<>();
    private List<Group> grupos;
    private Subject subject;

    private Long subjectId;
    private LazyUserMemberGroupDataModel lazyDataModel;

    public SecurityGroupUserHome() {
    }

    @PostConstruct
    public void init() {
        setSubject(subjectService.createInstance());
        identityManager = partitionManager.createIdentityManager();
        relationshipManager = partitionManager.createRelationshipManager();
        securityGroupService.setIdentityManager(identityManager);
        securityGroupService.setRelationshipManager(relationshipManager);
//        this.selectedGroups = new ArrayList<>();

    }

    public void asignarGruposUsuarios() {
        User user = BasicModel.getUser(identityManager, this.subject.getUsername());
        this.selectedGroups.stream().forEach((g) -> {
            try {
                if (!BasicModel.isMember(relationshipManager, user, g)) {
                    this.userTransaction.begin();
                    relationshipManager.add(new GroupMembership(user, g));
                    this.userTransaction.commit();
                    addSuccessMessage(I18nUtil.getMessages("user.member.group.add"), I18nUtil.getMessages("user.member.group.add"));
                }
            } catch (IdentityManagementException | NotSupportedException | SystemException |
                    HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException | RollbackException e) {
                java.util.logging.Logger.getLogger(SecurityGroupUserHome.class.getName()).log(Level.SEVERE, null, e);
            }
        });
    }

    public void removerUserFromGroup(Group g) {
        try {
            identityManager = partitionManager.createIdentityManager();
            relationshipManager = partitionManager.createRelationshipManager();
            User user = BasicModel.getUser(identityManager, this.subject.getUsername());
            this.userTransaction.begin();
            BasicModel.removeFromGroup(relationshipManager, user, g);
            this.userTransaction.commit();
            addSuccessMessage(I18nUtil.getMessages("subject.removeGroup"), I18nUtil.getMessages("subject.removeGroup"));
        } catch (IdentityManagementException | NotSupportedException | SystemException | RollbackException | HeuristicMixedException | HeuristicRollbackException | SecurityException | IllegalStateException e) {
            java.util.logging.Logger.getLogger(SecurityGroupUserHome.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    @Override
    public void handleReturn(SelectEvent event) {

    }

    @Override
    public org.jpapi.model.Group getDefaultGroup() {
        return this.defaultGroup;
    }

    @Override
    public List<org.jpapi.model.Group> getGroups() {
        if (groups.isEmpty() && subject.isPersistent()) {
            groups = groupService.findByOwnerAndModuleAndType(subject, "admin", org.jpapi.model.Group.Type.LABEL);
        }

        return groups;
    }

    public List<BussinesEntity> getSelectedSubjects() {
        return selectedSubjects;
    }

    public void setSelectedSubjects(List<BussinesEntity> selectedSubjects) {
        this.selectedSubjects = selectedSubjects;
    }

    public void selectListener(SelectEvent event) {
        System.out.println(event.getObject());
    }

    public Group[] getGruposSeleccionados() {
        List<Group> gruposAll = securityGroupService.find();
        User user = BasicModel.getUser(identityManager, getSubject().getUsername());
        List<Group> groupsUser = securityGroupService.find(user);
        List<Group> result = ListUtils.intersection(groupsUser, gruposAll);
        this.gruposSeleccionados = new Group[result.size()];
        for (int i = 0; i < this.gruposSeleccionados.length; i++) {
            gruposSeleccionados[i] = result.get(i);
        }
//        this.gruposSeleccionados.addAll(ListUtils.intersection(groupsUser, gruposAll));
        return gruposSeleccionados;
    }

    public void setGruposSeleccionados(Group[] gruposSeleccionados) {
        this.gruposSeleccionados = gruposSeleccionados;
    }

    public List<Group> completeGroup(String query) {
        List<Group> gruposAll = securityGroupService.find();
        User user = BasicModel.getUser(identityManager, getSubject().getUsername());
        List<Group> groupsUser = securityGroupService.find(user);
        List<Group> groupsSelected = new ArrayList<>();

        gruposAll.stream().filter((group) -> (!groupsUser.contains(group))).forEach((group) -> {
            groupsSelected.add(group);
        });

        List<Group> filteredGroup = new ArrayList<>();
        groupsSelected.stream().filter((g) -> (g.getName().toLowerCase().startsWith(query))).forEach((g) -> {
            filteredGroup.add(g);
        });

        return filteredGroup;
    }

    public boolean mostrarFormularioAsignarGruposUsuario() {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("profileSummary", getSubject());
        String width = settingHome.getValue(SettingNames.POPUP_SMALL_WIDTH, "400");
        String height = settingHome.getValue(SettingNames.POPUP_SMALL_HEIGHT, "240");
        super.openDialog(SettingNames.POPUP_SELECCIONAR_GRUPOS_USUARIO, width, height, true);
        return true;
    }

    public Subject getSubject() {
        if (subjectId != null && !this.subject.isPersistent()) {
            this.subject = subjectService.find(subjectId);
        }
        return subject;
    }

    public void setSubject(Subject subjectEdit) {
        this.subject = subjectEdit;
    }

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public LazyUserMemberGroupDataModel getLazyDataModel() {

        filter();

        return lazyDataModel;
    }

    public void filter() {
        if (lazyDataModel == null) {
            User user = BasicModel.getUser(identityManager, getSubject().getUsername());
            lazyDataModel = new LazyUserMemberGroupDataModel(securityGroupService, user);
        }

        lazyDataModel.setFilterValue(getKeyword());
    }

    public void setLazyDataModel(LazyUserMemberGroupDataModel lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public List<Group> getGrupos() {
//        List<Group> gruposAll = securityGroupService.find();
//        User user = BasicModel.getUser(identityManager, getSubject().getUsername());
//        List<Group> groupsUser = securityGroupService.find(user);
//        this.groupsSelected = new ArrayList<>();
//
//        gruposAll.stream().filter((group) -> (!groupsUser.contains(group))).forEach((group) -> {
//            groupsSelected.add(group);
//        });
        this.grupos = securityGroupService.find();
        return grupos;
    }

    public void setGrupos(List<Group> grupos) {
        this.grupos = grupos;
    }

    public List<Group> getSelectedGroups() {
        if (selectedGroups.isEmpty() && subject.isPersistent()) {
            User user = BasicModel.getUser(identityManager, getSubject().getUsername());
            selectedGroups = (securityGroupService.find(user));
        }
        return selectedGroups;
    }

    public void setSelectedGroups(List<Group> selectedGroups) {
        this.selectedGroups = selectedGroups;
    }

}
