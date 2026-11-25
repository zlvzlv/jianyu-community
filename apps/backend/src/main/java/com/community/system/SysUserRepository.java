package com.community.system;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 【临时脚手架】最小用户查询。包 1 的 SysUserService 落地后由它替换。
 */
@Repository
public class SysUserRepository {

    private static final RowMapper<SysUser> ROW_MAPPER = (rs, rowNum) -> new SysUser(
            rs.getLong("id"),
            rs.getString("username"),
            rs.getString("nickname"),
            rs.getString("avatar_url"),
            rs.getString("bio"),
            rs.getString("status"));

    private final JdbcTemplate jdbcTemplate;

    public SysUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<SysUser> findById(long id) {
        List<SysUser> users = jdbcTemplate.query(
                "SELECT id, username, nickname, avatar_url, bio, status FROM sys_user WHERE id = ?",
                ROW_MAPPER, id);
        return users.stream().findFirst();
    }

    public Optional<SysUser> findByUsername(String username) {
        List<SysUser> users = jdbcTemplate.query(
                "SELECT id, username, nickname, avatar_url, bio, status FROM sys_user WHERE username = ?",
                ROW_MAPPER, username);
        return users.stream().findFirst();
    }
}
