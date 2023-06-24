package TaskManagerProject;

import utils.CollectionUtils;
import utils.MailUtils;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class TaskDBHelper {
    public static void createTask(Task task) {

        Connection con = Connections.getConnection();
        PreparedStatement ps;
        int lastIndexId;
        try {
            ps = con.prepareStatement(Queries.CREATE_TASK_SQL_QUERY);
            ps.setString(1, task.getTaskName());
            ps.setString(2, task.getTaskDescription());
            ps.setString(3, task.getTag());
            ps.setInt(4, task.getPriorityLevel());
            ps.setDate(5, Date.valueOf(task.getDueDate()));
            ps.setLong(6, task.getAssignedUserId());
            ps.setLong(7, task.getCreatedByUserId());
            ps.setString(8, task.getCategory().toString());
            ps.setString(9, task.getStatus().toString());
            ps.executeUpdate();
            ps.close();
            con.close();
            if (CollectionUtils.isNotEmpty(task.getPreRequisites())) {
                ps = con.prepareStatement("select MAX(task_id) as last_id FROM task_management_system.task;");
                ResultSet rset = ps.executeQuery();
                ps.close();
                con.close();

                if (rset.next()) {
                    addPreRequisiteTask(rset.getLong("last_id"), task.getPreRequisites());
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<Task> showTasks(long userId) {
        ArrayList<Task> tasks = new ArrayList<>();
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM task_management_system.task where assigned_user_id=?");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ArrayList<PreRequisite> pre_tasks = getListOfPreTask(rs.getLong("task_id"));
                Task task = new Task(rs.getLong("task_id"), rs.getString("task_name"), rs.getString("description"),
                        rs.getString("tag"), rs.getDate("due_date").toLocalDate(), rs.getInt("priority_level"),
                        pre_tasks, rs.getLong("assigned_user_id"), rs.getLong("created_by_user_id"), rs.getString("category"),
                        rs.getString("status"));
                tasks.add(task);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;

    }

    // TODO: rename (ALl) to getTasksFilteredByStatus
    public static ArrayList<Task> showTaskFilteredByStatus(long userId, String status) {
        ArrayList<Task> tasks = new ArrayList<>();
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM task_management_system.task where assigned_user_id r ? AND status like ?");
            ps.setLong(1, userId);
            ps.setString(2, status);

            // TODO: Create common function to execute prepared statement and then convert the result to a Task object
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ArrayList<PreRequisite> pre_tasks = getListOfPreTask(rs.getLong("task_id"));
                System.out.println(pre_tasks.toString());
                Task task = new Task(rs.getLong("task_id"), rs.getString("task_name"), rs.getString("description"),
                        rs.getString("tag"), rs.getDate("due_date").toLocalDate(), rs.getInt("priority_level"),
                        pre_tasks, rs.getLong("assigned_user_id"), rs.getLong("created_by_user_id"), rs.getString("category"),
                        rs.getString("status"));

                tasks.add(task);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;

    }

    // TODO: rename to getTasksSortedByPriority
    public static ArrayList<Task> showTaskFilteredByPriority(long userId) {
        ArrayList<Task> tasks = new ArrayList<>();
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM task_management_system.task where assigned_user_id=? ORDER BY priority_level DESC");
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ArrayList<PreRequisite> pre_tasks = getListOfPreTask(rs.getLong("task_id"));
                Task task = new Task(rs.getLong("task_id"), rs.getString("task_name"), rs.getString("description"),
                        rs.getString("tag"), rs.getDate("due_date").toLocalDate(), rs.getInt("priority_level"),
                        pre_tasks, rs.getLong("assigned_user_id"), rs.getLong("created_by_user_id"), rs.getString("category"),
                        rs.getString("status"));
                tasks.add(task);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    // TODO: rename to getTasksSortedByDeadline
    // TODO: Create order enum (DESC, ASC)
    public static ArrayList<Task> showTaskFilteredByDeadline(long userId, String order) {
        ArrayList<Task> tasks = new ArrayList<>();
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM task_management_system.task where assigned_user_id=? ORDER BY due_date ?");
            ps.setLong(1, userId);
            ps.setString(2, order);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ArrayList<PreRequisite> pre_tasks = getListOfPreTask(rs.getLong("task_id"));
                Task task = new Task(rs.getLong("task_id"), rs.getString("task_name"), rs.getString("description"),
                        rs.getString("tag"), rs.getDate("due_date").toLocalDate(), rs.getInt("priority_level"),
                        pre_tasks, rs.getLong("assigned_user_id"), rs.getLong("created_by_user_id"), rs.getString("category"),
                        rs.getString("status"));
                tasks.add(task);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;

    }

    // TODO: rename to getListOfPreTaskIds
    public static ArrayList<PreRequisite>  getListOfPreTask(long taskId) {
        ArrayList<Integer> listOfPreRequestId= new ArrayList<>();
        ArrayList<PreRequisite>  pre_task = new ArrayList<>();
        PreRequisite task=null;
        Connection con = Connections.getConnection();
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("SELECT pre_request_task_id FROM task_management_system.pre_request_task where task_id=?");
            ps.setLong(1, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listOfPreRequestId.add(rs.getInt("pre_request_task_id"));
            }
            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(listOfPreRequestId.size()>0) {
            try {
                String sql = "SELECT task_id, task_name FROM task_management_system.task where task_id IN (";
                for (int i = 0; i < listOfPreRequestId.size(); i++) {
                    sql += (i == 0 ? "?" : ", ?");
                }
                sql += ")";
                ps = con.prepareStatement(sql);
                for (int i = 0; i < listOfPreRequestId.size(); i++) {
                    ps.setInt(i + 1, listOfPreRequestId.get(i));
                }

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    task = new PreRequisite(rs.getLong("task_id"), rs.getString("task_name"));
                    pre_task.add(task);
                }
                rs.close();
                ps.close();
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }else {
            try {
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        return pre_task;
    }

    public static void updateTaskName(long taskId, String taskName) {
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE task_management_system.task SET task_name=? WHERE task_id=?");
            ps.setString(1, taskName);
            ps.setLong(2, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTaskDescription(long taskId, String taskDescription) {

        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE task_management_system.task SET description=? WHERE task_id=?");
            ps.setString(1, taskDescription);
            ps.setLong(2, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTaskStatus(long taskId, String taskStatus) {

        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE task_management_system.task SET status=? WHERE task_id=?");
            ps.setString(1, taskStatus);
            ps.setLong(2, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTaskCategory(long taskId, String taskCategory) {
        try {
            Category.valueOf(taskCategory);
        } catch (Exception e) {
            throw new RuntimeException("Please provide valid category!");
        }

        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE task_management_system.task SET category=? WHERE task_id=?");
            ps.setString(1, taskCategory);
            ps.setLong(2, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTaskPriorityLevel(long taskId, int priorityLevel) {

        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE task_management_system.task SET priority_level=? WHERE task_id=?");
            ps.setInt(1, priorityLevel);
            ps.setLong(2, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateTaskDeadline(long taskId, LocalDate deadline) {
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE task_management_system.task SET due_date=? WHERE task_id=?");
            ps.setDate(1, Date.valueOf(deadline));
            ps.setLong(2, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updatePreRequisiteTask(long taskId, ArrayList<PreRequisite> preRequisites) {

        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT  pre_request_task_id FROM task_management_system.pre_request_task where task_id=?");
            ps.setLong(1, taskId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                deletePreRequisiteTask(taskId);
            }
            addPreRequisiteTask(taskId, preRequisites);
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPreRequisiteTask(long taskId, ArrayList<PreRequisite> preRequisites) {
        for (PreRequisite task : preRequisites) {
            Connection con = Connections.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO task_management_system.pre_request_task (task_id, pre_request_task_id) VALUES(?, ?)");
                ps.setLong(1, taskId);
                ps.setLong(2, task.getPreTaskId());
                System.out.println(ps.executeUpdate());
                ps.close();
                con.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void deletePreRequisiteTask(long taskId) {
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM task_management_system.pre_request_task WHERE task_id=?");
            ps.setLong(1, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void updateAssignedUser(long taskId, long assignedUserId) {

        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("UPDATE task_management_system.task SET assigned_user_id=? WHERE task_id=?");
            ps.setLong(1, assignedUserId);
            ps.setLong(2, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO: use soft delete instead of hard delete
    public static void deleteTask(Long taskId) {
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("DELETE FROM task_management_system.task WHERE task_id=?");
            ps.setLong(1, taskId);
            ps.executeUpdate();
            ps.close();
            con.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isResevedNotification(long user_id) {
        boolean isResevedNotification = false;
        LocalDate todayDate = LocalDate.now();
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT notification_id, send, `date`, user_id FROM task_management_system.notification WHERE user_id=? and date=? ");
            ps.setLong(1, user_id);
            ps.setDate(2, Date.valueOf(todayDate));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                isResevedNotification = true;
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return isResevedNotification;
    }

    public static ArrayList<Task> sendNotificationAndEmail(long user_id) {
        ArrayList<Task> tasks = new ArrayList<>();

        LocalDate todayDate = LocalDate.now();
        LocalDate nextdate = todayDate.plusDays(1);
        User user = UserDbHelper.getUserByID(user_id);
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT * FROM task_management_system.task where assigned_user_id=? and due_date=? or due_date=? ");
            ps.setLong(1, user_id);
            ps.setDate(2, Date.valueOf(todayDate));
            ps.setDate(3, Date.valueOf(nextdate));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ArrayList<PreRequisite> pre_tasks = getListOfPreTask(rs.getLong("task_id"));
                Task task = new Task(rs.getLong("task_id"), rs.getString("task_name"), rs.getString("description"),
                        rs.getString("tag"), rs.getDate("due_date").toLocalDate(), rs.getInt("priority_level"),
                        pre_tasks, rs.getLong("assigned_user_id"), rs.getLong("created_by_user_id"), rs.getString("category"),
                        rs.getString("status"));
                tasks.add(task);

            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (!isResevedNotification(user_id)) {
            MailUtils.sendEmail(user.getEmail(), "Tasks to be delivered today and tomorrow", MailUtils.buildTaskAssignedBody(user.getFirstName(), tasks));
        }
        try {
            PreparedStatement ps = con.prepareStatement("INSERT INTO task_management_system.notification (send, `date`, user_id) VALUES(?, ?, ?) ");
            ps.setBoolean(1, true);
            ps.setDate(2, Date.valueOf(todayDate));
            ps.setLong(3, user_id);
            ps.executeUpdate();
            ps.close();
            con.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return tasks;

    }
    
    public static long getTaskIdByTaskName(String taskName) {
        long taskId = 0;
        Connection con = Connections.getConnection();
        try {
            PreparedStatement ps = con.prepareStatement("SELECT task_id FROM task_management_system.task WHERE task_name=?");
            ps.setString(1, taskName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                taskId = rs.getLong("task_id");
            }
            ps.close();
            con.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return taskId;
    }

    
}
