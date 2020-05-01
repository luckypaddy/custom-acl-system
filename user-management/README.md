# User Management

User management library based with simple role hierarchy without separated permissions, for example:

    ADMIN > REVIEWER > USER

    USER is a base role

    REVIEWER has authority of USER

    ADMIN has authoruty of REVIEWER
    
[Nested Set Model](https://en.wikipedia.org/wiki/Nested_set_model)  was taken with the assumption that structure of roles hierarchy is stable and changes rarely.