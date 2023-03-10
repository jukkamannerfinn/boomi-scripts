CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `admin`@`%` 
    SQL SECURITY DEFINER
VIEW `posti_users_view` AS
    SELECT 
        `a`.`user_id` AS `USERID`,
        DATE_FORMAT(`b`.`date_of_birth`, '%Y%m%d') AS `BDATE`,
        `a`.`first_name` AS `FNAME`,
        `a`.`last_name` AS `LNAME`,
        REGEXP_REPLACE(`b`.`home_address_line1`, '(^(-)+)', '') AS `HSTREET`,
        (CASE
            WHEN
                (REGEXP_REPLACE(`b`.`home_city`,
                        '((^-)|[.]|[[:space:]]|[[:digit:]])',
                        '') = '')
            THEN
                `b`.`home_postalcode`
            ELSE REGEXP_REPLACE(`b`.`home_postalcode`,
                    '((-)+|[.]|!.[[:space:]]|([[:alpha:]]))',
                    '')
        END) AS `HZIP`,
        REGEXP_REPLACE(`b`.`home_city`,
                '((^(-)+)|(--)+|[.]|[[:space:]]|[[:digit:]])',
                '') AS `HCITY`
    FROM
        (`users` `a`
        JOIN `appended_user_profile` `b`)
    WHERE
        ((`a`.`user_id` = `b`.`user_id`)
            AND (`b`.`home_address_line1` IS NOT NULL)
            AND (REGEXP_REPLACE(`b`.`home_address_line1`,
                '((^-)|[[:space:]]|Default)',
                '') > '')
            AND ((`b`.`home_postalcode` IS NOT NULL)
            OR (`b`.`home_city` IS NOT NULL))
            AND (`b`.`home_city` <> 'Default')
            AND (`b`.`home_country` = 'FI')
            AND (`b`.`member_activity_status_18m` NOT IN ('Deleted' , 'Deceased'))
            AND ((`a`.`modifieddate` BETWEEN ((LAST_DAY(NOW()) + INTERVAL 1 DAY) + INTERVAL -(2) MONTH) AND ((LAST_DAY(NOW()) + INTERVAL 1 DAY) + INTERVAL -(1) MONTH))
            OR (`b`.`modifieddate` BETWEEN ((LAST_DAY(NOW()) + INTERVAL 1 DAY) + INTERVAL -(2) MONTH) AND ((LAST_DAY(NOW()) + INTERVAL 1 DAY) + INTERVAL -(1) MONTH))))