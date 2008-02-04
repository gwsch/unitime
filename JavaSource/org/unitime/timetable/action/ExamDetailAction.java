package org.unitime.timetable.action;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.commons.Debug;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.form.ExamEditForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.DistributionPref;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.LookupTables;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.DistributionPrefsTableBuilder;
import org.unitime.timetable.webutil.Navigation;

public class ExamDetailAction extends PreferencesAction {
    
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ExamEditForm frm = (ExamEditForm) form;
        try {
            
            // Set common lookup tables
            super.execute(mapping, form, request, response);
            
            HttpSession httpSession = request.getSession();
            User user = Web.getUser(httpSession);
            Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
            MessageResources rsc = getResources(request);
            ActionMessages errors = new ActionMessages();
            
            //Read parameters
            String examId = (request.getParameter("examId")==null) ? (request.getAttribute("instructorId")==null) ? null : request.getAttribute("examId").toString() : request.getParameter("examId");
            
            String op = frm.getOp();
            String deleteType = request.getParameter("deleteType");
            
            if (request.getParameter("op2")!=null && request.getParameter("op2").length()>0)
                op = request.getParameter("op2");
            
            if (request.getAttribute("fromChildScreen")!=null && request.getAttribute("fromChildScreen").toString().equals("true") ) {
                op = "";
                frm.setOp(op);
            }
            
            // Read instructor id from form
            if (op.equals(rsc.getMessage("button.editInfo")) || op.equals(rsc.getMessage("button.next")) || op.equals(rsc.getMessage("button.previous"))) {
                examId = frm.getExamId();
            } else {
                frm.reset(mapping, request);
            }
            
            //Check op exists
            if (op==null) throw new Exception ("Null Operation not supported.");
            
            Debug.debug("op: " + op);
            Debug.debug("exam: " + examId);
            
            //Check exam exists
            if (examId==null || examId.trim()=="") throw new Exception ("Exam Info not supplied.");
            
            // Cancel - Go back to Instructors List Screen
            if(op.equals(rsc.getMessage("button.back")) && examId!=null && examId.trim()!="") {
                response.sendRedirect(response.encodeURL("examList.do"));
                request.setAttribute("hash", examId);
                return null;
            }
            
            Exam exam = new ExamDAO().get(Long.valueOf(examId));
            
            //Edit Information - Redirect to info edit screen
            if (op.equals(rsc.getMessage("button.edit")) && examId!=null && examId.trim()!="") {
                response.sendRedirect( response.encodeURL("examEdit.do?examId="+examId) );
                return null;
            }
            
            if (op.equals(rsc.getMessage("button.next"))) {
                response.sendRedirect(response.encodeURL("examDetail.do?examId="+frm.getNextId()));
                return null;
            }
            
            if (op.equals(rsc.getMessage("button.previous"))) {
                response.sendRedirect(response.encodeURL("examDetail.do?instructorId="+frm.getPreviousId()));
                return null;
            }
            
            // Load form attributes that are constant
            doLoad(request, frm, exam);
            
            // Display distribution Prefs
            DistributionPrefsTableBuilder tbl = new DistributionPrefsTableBuilder();
            String html = tbl.getDistPrefsTableForExam(request, exam, true);
            if (html!=null)
                request.setAttribute(DistributionPref.DIST_PREF_REQUEST_ATTR, html);
            
            if (!exam.getOwners().isEmpty()) {
                WebTable table = new WebTable(4, null, new String[] {"Object", "Type", "Manager", "Students", "Assignment"}, new String[] {"left", "center", "left", "center", "left"}, new boolean[] {true, true, true, true, true});
                for (Iterator i=new TreeSet(exam.getOwners()).iterator();i.hasNext();) {
                    ExamOwner owner = (ExamOwner)i.next();
                    String onclick = null, name = null, type = null, students = String.valueOf(owner.getStudents().size()), manager = null, assignment = null;
                    switch (owner.getOwnerType()) {
                        case ExamOwner.sOwnerTypeClass :
                            Class_ clazz = (Class_)owner.getOwnerObject();
                            onclick = "onClick=\"document.location='classDetail.do?cid="+clazz.getUniqueId()+"';\"";
                            name = clazz.getClassLabel();
                            type = "Class";
                            manager = clazz.getManagingDept().getShortLabel();
                            if (clazz.getCommittedAssignment()!=null)
                                assignment = clazz.getCommittedAssignment().getPlacement().getLongName();
                            break;
                        case ExamOwner.sOwnerTypeConfig :
                            InstrOfferingConfig config = (InstrOfferingConfig)owner.getOwnerObject();
                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+config.getInstructionalOffering().getUniqueId()+"';\"";;
                            name = config.getCourseName()+" ["+config.getName()+"]";
                            type = "Configuration";
                            manager = config.getInstructionalOffering().getControllingCourseOffering().getDepartment().getShortLabel();
                            break;
                        case ExamOwner.sOwnerTypeOffering :
                            InstructionalOffering offering = (InstructionalOffering)owner.getOwnerObject();
                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+offering.getUniqueId()+"';\"";;
                            name = offering.getCourseName();
                            type = "Offering";
                            manager = offering.getControllingCourseOffering().getDepartment().getShortLabel();
                            break;
                        case ExamOwner.sOwnerTypeCourse :
                            CourseOffering course = (CourseOffering)owner.getOwnerObject();
                            onclick = "onClick=\"document.location='instructionalOfferingDetail.do?io="+course.getInstructionalOffering().getUniqueId()+"';\"";;
                            name = course.getCourseName();
                            type = "Course";
                            manager = course.getDepartment().getShortLabel();
                            break;
                                
                    }
                    table.addLine(onclick, new String[] { name, type, manager, students, assignment}, null);
                }
                request.setAttribute("ExamDetail.table",table.printTable());
            }
            
            if (exam.getAssignedPeriod()!=null) {
                String assignment = "<tr><td>Examination Period:</td><td>"+exam.getAssignedPeriod().getName()+"</td></tr>";
                if (!exam.getAssignedRooms().isEmpty()) {
                    assignment += "<tr><td>Room"+(exam.getAssignedRooms().size()>1?"s":"")+":</td><td>";
                    for (Iterator i=new TreeSet(exam.getAssignedRooms()).iterator();i.hasNext();) {
                        Location location = (Location)i.next();
                        assignment += location.getLabel();
                        if (i.hasNext()) assignment+="<br>";
                    }
                    assignment += "</td></tr>";
                }
                request.setAttribute("ExamDetail.assignment",assignment);
            }
            
            BackTracker.markForBack(
                    request,
                    "examDetail.do?examId=" + examId,
                    "Exam ("+ (frm.getName()==null?frm.getLabel().trim():frm.getName().trim()) +")",
                    true, false);
            
            // Initialize Preferences for initial load
            frm.setAvailableTimePatterns(null);
            initPrefs(user, frm, exam, null, false);
            
            // Process Preferences Action
            processPrefAction(request, frm, errors);
            
            setupInstructors(request, frm, exam);
            
            LookupTables.setupRooms(request, exam);      // Room Prefs
            LookupTables.setupBldgs(request, exam);      // Building Prefs
            LookupTables.setupRoomFeatures(request, exam); // Preference Levels
            LookupTables.setupRoomGroups(request, exam);   // Room Groups
            
            Long nextId = Navigation.getNext(request.getSession(), Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
            Long prevId = Navigation.getPrevious(request.getSession(), Navigation.sInstructionalOfferingLevel, exam.getUniqueId());
            frm.setPreviousId(prevId==null?null:prevId.toString());
            frm.setNextId(nextId==null?null:nextId.toString());
            
            return mapping.findForward("showExamDetail");
            
        } catch (Exception e) {
            Debug.error(e);
            throw e;
        }
    }
    
    private void doLoad(HttpServletRequest request, ExamEditForm frm, Exam exam) {
        frm.setExamId(exam.getUniqueId().toString());
        
        frm.setLabel(exam.getLabel());
        frm.setName(exam.getName());
        frm.setNote(exam.getNote());
        frm.setLength(exam.getLength());
        frm.setSeatingType(Exam.sSeatingTypes[exam.getSeatingType()]);
        frm.setMaxNbrRooms(exam.getMaxNbrRooms());
        
        TreeSet instructors = new TreeSet(exam.getInstructors());

        for (Iterator i = instructors.iterator(); i.hasNext(); ) {
            DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
            frm.getInstructors().add(instr.getUniqueId().toString());
        }
        
        User user = Web.getUser(request.getSession());

        frm.setEditable(exam.isEditableBy(user));
    }

    protected void setupInstructors(HttpServletRequest request, ExamEditForm frm, Exam exam) throws Exception {

        List instructors = frm.getInstructors();
        if(instructors.size()==0) return;
        
        HashSet deptIds = new HashSet();
        
        for (Iterator i = exam.getInstructors().iterator(); i.hasNext(); ) {
            DepartmentalInstructor instr = (DepartmentalInstructor)i.next();
            deptIds.add(instr.getDepartment().getUniqueId());
        }
        for (Iterator i = exam.getOwners().iterator(); i.hasNext(); ) {
            ExamOwner own = (ExamOwner)i.next();
            deptIds.add(own.getCourse().getDepartment().getUniqueId());
        }
        
        Long[] deptsIdsArray = new Long[deptIds.size()]; int idx = 0;
        for (Iterator i=deptIds.iterator();i.hasNext();idx++)
            deptsIdsArray[idx++]=(Long)i.next();

        LookupTables.setupInstructors(request, deptsIdsArray);
        Vector deptInstrList = (Vector) request.getAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME);

        // For each instructor set the instructor list
        for (int i=0; i<instructors.size(); i++) {
            request.setAttribute(DepartmentalInstructor.INSTR_LIST_ATTR_NAME + i, deptInstrList);
        }
    }
}
