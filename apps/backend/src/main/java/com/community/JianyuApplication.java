package com.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 见屿（jianyu）后端启动类。
 *
 * <p>本周（阶段 1）只包含「社交与好友圈」的最小闭环：关注/取关 + 个人主页。
 * 包 1 的正式骨架（Spring Security + JWT + 5 表 RBAC + Knife4j）落地后，
 * 本类的职责不变，社交模块代码可直接平移。</p>
 */
@SpringBootApplication
public class JianyuApplication {

    public static void main(String[] args) {
        SpringApplication.run(JianyuApplication.class, args);
    }
}
