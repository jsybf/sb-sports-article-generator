CREATE TABLE IF NOT EXISTS sports_match(
  sports_match_id INT AUTO_INCREMENT,
  sports    VARCHAR(50) NOT NULL,
  league    VARCHAR(10) NOT NULL,
  home_team VARCHAR(50) NOT NULL,
  away_team VARCHAR(50) NOT NULL,
  match_at DATETIME NOT NULL,
  updated_at TIMESTAMP NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY(sports_match_id),
  UNIQUE (sports, league, home_team, away_team, match_at)
);

CREATE TABLE IF NOT EXISTS pick(
  pick_id INT AUTO_INCREMENT,
  sports_match_id INT NOT NULL,
  updated_at TIMESTAMP NOT NULL default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  content TEXT NOT NULL,
  input_tokens INT NOT NULL,
  output_tokens INT NOT NULL,

  PRIMARY KEY(pick_id),
  FOREIGN KEY (sports_match_id) REFERENCES sports_match (sports_match_id) ON DELETE CASCADE
);
