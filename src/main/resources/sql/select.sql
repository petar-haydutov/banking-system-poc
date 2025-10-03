SELECT a.id, a.first_name, a.last_name, SUM(t.amount) AS total_transferred
FROM accounts a
         INNER JOIN transfers t ON a.id = t.source_id
WHERE t.transfer_time >= '2019-01-01'
GROUP BY a.id, a.first_name, a.last_name
HAVING total_transferred > 1000
ORDER BY total_transferred DESC;