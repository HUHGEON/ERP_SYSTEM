package com.example.swing;

import com.example.swing.panel.*;

import javax.swing.*;

public class MainFrame extends JFrame {

    public MainFrame() {
        setTitle("인사관리 시스템");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        tabs.addTab("직원", new EmployeePanel());
        tabs.addTab("경력", new CareerPanel());
        tabs.addTab("인사기록", new HrRecordPanel());
        tabs.addTab("휴가기록", new LeavePanel());
        tabs.addTab("프로젝트", new ProjectPanel());
        tabs.addTab("프로젝트투입", new ProjectParticipationPanel());
        tabs.addTab("평가", new EvaluationPanel());
        tabs.addTab("고객평가", new CustomerEvaluationPanel());
        tabs.addTab("PM평가", new PmEvaluationPanel());
        tabs.addTab("동료평가", new PartnerEvaluationPanel());
        tabs.addTab("스터디", new StudyPanel());
        tabs.addTab("스터디활동", new StudyActivityHistoryPanel());

        add(tabs);
    }
}
