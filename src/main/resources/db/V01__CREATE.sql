CREATE TABLE "User" (
  "id" SERIAL PRIMARY KEY,
  "name" VARCHAR(255) NOT NULL,
  "email" VARCHAR(255) NOT NULL,
  UNIQUE ("email")
);

CREATE TABLE "Post" (
  "id" SERIAL PRIMARY KEY,
  "userId" BIGINT NOT NULL,
  "title" VARCHAR(255) NOT NULL,
  FOREIGN KEY ("userId") REFERENCES "User" ("id")
);