package com.community.module.social.repository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 关注关系数据访问层（表 {@code user_follows}，脚本 V20261125_01）。
 *
 * <p>所有 SQL 只用 PostgreSQL 与 H2 兼容的写法，方便本周用 H2 演示、正式环境直接跑 PostgreSQL。</p>
 */
@Repository
public class UserFollowRepository {

    private static final RowMapper<FollowRow> FOLLOW_ROW_MAPPER = (rs, rowNum) -> new FollowRow(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("nickname"),
            rs.getString("avatar_url"),
            rs.getString("bio"),
            rs.getBoolean("following"),
            rs.getBoolean("mutual"));

    /** 列表查询公共列：用户基本信息 + 访问者是否已关注该用户（v 为访问者→该用户的关注行）。 */
    private static final String FOLLOW_ROW_COLUMNS = """
            u.id,
            u.username,
            u.nickname,
            u.avatar_url,
            u.bio,
            CASE WHEN v.follower_id IS NULL THEN FALSE ELSE TRUE END AS following,
            COALESCE(v.is_mutual, FALSE) AS mutual
            """;

    private final JdbcTemplate jdbcTemplate;

    public UserFollowRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean exists(long followerId, long followeeId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_follows WHERE follower_id = ? AND followee_id = ?",
                Integer.class, followerId, followeeId);
        return count != null && count > 0;
    }

    /**
     * 幂等插入：已存在关注关系时返回 false，不抛异常。
     *
     * <p>并发下依赖唯一约束 {@code uk_user_follows_pair} 兜底，捕获重复键后视为已关注。</p>
     */
    public boolean insertIfAbsent(long followerId, long followeeId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO user_follows (follower_id, followee_id, is_mutual) VALUES (?, ?, FALSE)",
                    followerId, followeeId);
            return true;
        } catch (DuplicateKeyException ex) {
            return false;
        }
    }

    public boolean delete(long followerId, long followeeId) {
        return jdbcTemplate.update(
                "DELETE FROM user_follows WHERE follower_id = ? AND followee_id = ?",
                followerId, followeeId) > 0;
    }

    /** 写入单条关注关系的互关标记。 */
    public void updateMutual(long followerId, long followeeId, boolean mutual) {
        jdbcTemplate.update(
                "UPDATE user_follows SET is_mutual = ? WHERE follower_id = ? AND followee_id = ?",
                mutual, followerId, followeeId);
    }

    /** 当前关注关系的互关标记；无关注关系时返回 false。 */
    public boolean isMutual(long followerId, long followeeId) {
        List<Boolean> result = jdbcTemplate.query(
                "SELECT is_mutual FROM user_follows WHERE follower_id = ? AND followee_id = ?",
                (rs, rowNum) -> rs.getBoolean("is_mutual"),
                followerId, followeeId);
        return !result.isEmpty() && Boolean.TRUE.equals(result.get(0));
    }

    public long countFollowers(long userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_follows WHERE followee_id = ?", Long.class, userId);
        return count == null ? 0L : count;
    }

    public long countFollowing(long userId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM user_follows WHERE follower_id = ?", Long.class, userId);
        return count == null ? 0L : count;
    }

    /** 粉丝列表：关注 userId 的人。viewerId 为 0 表示游客。 */
    public List<FollowRow> pageFollowers(long userId, long viewerId, long offset, int limit) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM user_follows f
                        JOIN sys_user u ON u.id = f.follower_id
                        LEFT JOIN user_follows v ON v.follower_id = ? AND v.followee_id = u.id
                        WHERE f.followee_id = ?
                        ORDER BY f.created_at DESC, f.id DESC
                        LIMIT ? OFFSET ?
                        """.formatted(FOLLOW_ROW_COLUMNS),
                FOLLOW_ROW_MAPPER, viewerId, userId, limit, offset);
    }

    /** 关注列表：userId 关注的人。viewerId 为 0 表示游客。 */
    public List<FollowRow> pageFollowing(long userId, long viewerId, long offset, int limit) {
        return jdbcTemplate.query("""
                        SELECT %s
                        FROM user_follows f
                        JOIN sys_user u ON u.id = f.followee_id
                        LEFT JOIN user_follows v ON v.follower_id = ? AND v.followee_id = u.id
                        WHERE f.follower_id = ?
                        ORDER BY f.created_at DESC, f.id DESC
                        LIMIT ? OFFSET ?
                        """.formatted(FOLLOW_ROW_COLUMNS),
                FOLLOW_ROW_MAPPER, viewerId, userId, limit, offset);
    }
}
