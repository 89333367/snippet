package sunyu.demo.integration.bigdata.admin.pojo;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.HashSet;
import java.util.Set;

@TableName("shiro_subject")
public class ShiroSubject {
    @TableField(exist = false)
    private Set<String> roles = new HashSet<>();
    @TableField(exist = false)
    private Set<String> permissions = new HashSet<>();

    private Integer id;
    private String email;
    private String password;
    private String name;
    private Integer disabled = 0;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        if (StrUtil.isBlank(name)) {
            this.name = this.email;
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDisabled() {
        return disabled;
    }

    public void setDisabled(Integer disabled) {
        this.disabled = disabled;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
}
