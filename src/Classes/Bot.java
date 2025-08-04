package Classes;

import Listeners.NonSpecificSlashCommands.AdminCommands;
import Listeners.NonSpecificSlashCommands.FunCommands;
import Listeners.*;
import Listeners.NonSpecificSlashCommands.PrivateCommands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.List;
import java.util.Objects;

public class Bot {
    private static final TagRestrictorListener dog_tag_restrictor = new TagRestrictorListener();
    private static boolean is_tag_restrictor_active = true;
    private static boolean is_promotion_message_active = true;

    public static JDA jda;


    public static void init(String TOKEN) throws InterruptedException {

        JDABuilder builder = JDABuilder.createDefault(TOKEN)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_WEBHOOKS,
                        GatewayIntent.GUILD_MODERATION, GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING
                );

        jda = builder.addEventListeners(
                        dog_tag_restrictor, new WelcomeMessageListener(), new BotReadier(), new CommunityXPController(),
                        new AdminCommands(), new FunCommands(), new PrivateCommands(),
                        /*new HLListener(),*/ new MemberCounter(), new RecruitmentTicketController()
                )
                .build().awaitReady();

    }


    public static boolean setTagRestrictor(boolean set) {
        // if user wants to set listener to what it already set to, e.g. trying to turn the lights on when they're already on,
        // the program just returns from the function with the status, acting as if it really did set the listener, to prevent further issues
        if ((is_tag_restrictor_active && set) || (!is_tag_restrictor_active && !set)){
            return is_tag_restrictor_active;
        }

        if (set) {
            jda.addEventListener(dog_tag_restrictor);
            return is_tag_restrictor_active = true;
        }

        jda.removeEventListener(dog_tag_restrictor);
        return is_tag_restrictor_active = false;
    }

    public static boolean getTagRestrictorStatus(){
        return is_tag_restrictor_active;
    }

    public static boolean setPromotionMessageStatus(boolean new_is_promotion_message_active){
        return is_promotion_message_active = new_is_promotion_message_active;
    }

    public static boolean getPromotionMessageStatus(){
        return is_promotion_message_active;
    }


    public static void addMemberToDatabase(Member member) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
        System.out.println("connection made: add member to database");
        c.prepareStatement("INSERT INTO Members(ID, UserName) VALUES ("+member.getId()+", '"+member.getUser().getName()+"') ON CONFLICT(ID) DO UPDATE SET UserName='"+member.getUser().getName()+"';").execute();
        c.close();
        System.out.println("connection closed: add member to database");
    }

    public static void addMembersToDatabase(List<Member> members) throws ClassNotFoundException, SQLException {
        if (members.size() == 0)
            return;

        System.out.println("connection attempt: add members to database");
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
        System.out.println("connection made: add members to database");
        StringBuilder s = new StringBuilder("INSERT INTO Members(ID, UserName) VALUES");
        for (Member member : members)
            s.append(" ("+member.getId()+", '"+member.getUser().getName()+"'),");

        s.deleteCharAt(s.length()-1);
        s.append(" ON CONFLICT REPLACE");
        s.append(';');

        try {
            c.prepareStatement(s.toString()).execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        c.close();
        System.out.println("connection closed: add members to database");
    }


    public static void addXPToMember(String memberID, long XP) throws ClassNotFoundException, SQLException {
        System.out.println("connection attempt: add xp to member");
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
        try {
            DatabaseMetaData metaData = c.getMetaData();
            c.prepareStatement("UPDATE Members SET XP_accumulator = XP_accumulator + "+XP+" WHERE ID = "+memberID+";").execute();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        System.out.println("connection close: add xp to member");
        c.close();
    }

    public static void setMemberLevelling(String memberID, boolean levelling) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
        String sql = "UPDATE Members SET levelling_active = 0 WHERE ID = "+memberID+";";
        if (levelling) sql = "UPDATE Members SET levelling_active = 1 WHERE ID = "+memberID+";";

        c.prepareStatement(sql).execute();
        c.close();
    }

    // returns current member xp, returns -1 if it is disabled for them
    public static int getMemberXP(String memberID) throws ClassNotFoundException, SQLException {
        System.out.println("connection attempt: get xp from member");

        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
        PreparedStatement ps = c.prepareStatement("SELECT XP, XP_accumulator FROM Members WHERE ID = "+memberID+" AND levelling_active = 1;");
        ResultSet result = ps.executeQuery();

        int XP = result.getInt(1) + result.getInt(2);
        if (result.wasNull())
            XP = -1;
        result.close();
        ps.close();
        c.close();
        System.out.println("connection close: get xp from member");
        return XP;
    }

    // returns current member xp in their accumulator
    public static long getMemberAccumulatedXP(String memberID) {
        try {
            System.out.println("connection attempt: get xpacc from member");
            Class.forName("org.sqlite.JDBC");
            Connection c;
            c = DriverManager.getConnection("jdbc:sqlite:DDServerData.db");
            PreparedStatement ps = c.prepareStatement("SELECT XP_accumulator FROM Members WHERE ID = "+memberID+";");
            ResultSet result = ps.executeQuery();
            long XP = result.getInt(1);
            result.close();
            ps.close();
            c.close();
            System.out.println("connection close: get xpacc from member");
            return XP;
        } catch (SQLException | ClassNotFoundException e) {
            sendErrorMessage(e);
        }
        return 0;
    }

    // return current level, and xp to next level
    public static long[] getMemberLevel(long xp){
        long level = 0;
        long current_differential =  Config.START_LEVEL;
        while (Math.floorDiv(xp, current_differential) != 0){
            level += 1;
            xp -= current_differential;
            current_differential += Config.LEVEL_DIFF;
        }
        return new long[]{level, current_differential - xp};
    }

    public static void sendErrorMessage(Exception e) {
        e.printStackTrace();

//        if (jda == null || !jda.getStatus().equals(JDA.Status.CONNECTED) || Config.ERROR_MESSAGE_CHANNEL == null) {
//            e.printStackTrace();
//            return;
//        }
//
//        StringBuilder message = new StringBuilder();
//        message.append("## ");
//
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//        e.printStackTrace(pw);
//        message.append(sw);
//
//
//        Objects.requireNonNull(jda.getTextChannelById(Config.ERROR_MESSAGE_CHANNEL)).sendMessage(message.toString()).queue();
    }

    public static void pauseRuntime(long millis) {
        synchronized (Runtime.getRuntime()){
            try {
                Runtime.getRuntime().wait(millis);
            } catch (InterruptedException e) {
                sendErrorMessage(e);
            }
        }
    }

}
