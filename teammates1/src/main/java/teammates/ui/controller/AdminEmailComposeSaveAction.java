package teammates.ui.controller;

import java.util.ArrayList;
import java.util.List;

import com.google.appengine.api.datastore.Text;

import teammates.common.datatransfer.AdminEmailAttributes;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.common.util.StatusMessage;
import teammates.common.util.Const.StatusMessageColor;
import teammates.logic.api.GateKeeper;

public class AdminEmailComposeSaveAction extends Action {
    
    List<String> addressReceiver = new ArrayList<String>();
    List<String> groupReceiver = new ArrayList<String>();
    
    @Override
    protected ActionResult execute() {
        
        new GateKeeper().verifyAdminPrivileges(account);
        AdminEmailComposePageData data = new AdminEmailComposePageData(account);
        
        String emailContent = getRequestParamValue(Const.ParamsNames.ADMIN_EMAIL_CONTENT);
        String subject = getRequestParamValue(Const.ParamsNames.ADMIN_EMAIL_SUBJECT);
        String addressReceiverListString = getRequestParamValue(Const.ParamsNames.ADMIN_EMAIL_ADDRESS_RECEVIERS);
        
        String groupReceiverListFileKey = getRequestParamValue(Const.ParamsNames.ADMIN_EMAIL_GROUP_RECEIVER_LIST_FILE_KEY);    
        

        String emailId = getRequestParamValue(Const.ParamsNames.ADMIN_EMAIL_ID);
        
        addressReceiver.add(addressReceiverListString);
        
        if(groupReceiverListFileKey != null && !groupReceiverListFileKey.isEmpty()){
            groupReceiver.add(groupReceiverListFileKey);
        }
        
        boolean isNewDraft = (emailId == null);
        
        if(isNewDraft){
            //this is a new email draft, so create a new admin email entity
            createAndSaveNewDraft(subject, addressReceiver, groupReceiver, emailContent);
        } else {            
            //currently editing a previous email draft, so we need to update the previous draft
            //instead of creating a new admin email entity
            
            //retrieve the previous draft email
            AdminEmailAttributes previousDraft = logic.getAdminEmailById(emailId);
            
            if(previousDraft == null){
                //the previous draft is not found (eg. deleted by accident when editing) 
                createAndSaveNewDraft(subject, addressReceiver, groupReceiver, emailContent);
            } else {
                //the previous draft exists so simply update it with the latest email info
                updatePreviousEmailDraft(previousDraft.getEmailId(), subject, addressReceiver, groupReceiver, emailContent);
            }         
        }
        
        if(isError){
            data.emailToEdit = new AdminEmailAttributes(subject,
                                                        addressReceiver,
                                                        groupReceiver,
                                                        new Text(emailContent),
                                                        null);    
            data.emailToEdit.emailId = emailId;
        } else {
            statusToAdmin = "Email draft has been saved: <br>" +
                            "Subject: " + subject;
            statusToUser.add(new StatusMessage("Email draft has been saved", StatusMessageColor.SUCCESS)); 
        }
        
        return createShowPageResult(Const.ViewURIs.ADMIN_EMAIL, data);
    }
    
    private void updatePreviousEmailDraft(String previousEmailId,
                                          String subject, 
                                          List<String> addressReceiver,
                                          List<String> groupReceiver,
                                          String content
                                          ){
        
        AdminEmailAttributes newDraft = new AdminEmailAttributes(subject,
                                                                 addressReceiver,
                                                                 groupReceiver,
                                                                 new Text(content),
                                                                 null);
        try {
            logic.updateAdminEmailById(newDraft, previousEmailId);
        } catch (InvalidParametersException | EntityDoesNotExistException e) {
            isError = true;
            setStatusForException(e);
        }
        
    }
    
    private void createAndSaveNewDraft(String subject, 
                                       List<String> addressReceiver,
                                       List<String> groupReceiver,
                                       String content){
        
        AdminEmailAttributes newDraft = new AdminEmailAttributes(subject,
                                                                 addressReceiver,
                                                                 groupReceiver,
                                                                 new Text(content),
                                                                 null);
        try {
            logic.createAdminEmail(newDraft);
        } catch (InvalidParametersException e) {
            isError = true;
            setStatusForException(e, e.getMessage());
        }
    }

}
