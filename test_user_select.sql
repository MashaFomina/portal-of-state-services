SET profiling = 1;

# 0,0031
SELECT * FROM users WHERE user_type="CITIZEN" and id = 2;
SELECT * FROM users LEFT JOIN citizens AS c ON id = c.user WHERE user_type="CITIZEN" and id = 2;

# 0,0053
SELECT * FROM users
LEFT JOIN citizens AS c ON id = c.user
LEFT JOIN doctors AS d ON id = d.user
LEFT JOIN representatives AS r ON id = r.user
WHERE id = 2;

SHOW profiles;