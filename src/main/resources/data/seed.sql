-- Sample problems for development/testing

INSERT OR IGNORE INTO users (username, email, password, role)
VALUES ('admin', 'admin@codearena.com', '$2a$10$iyHiGiN36A2WlwBTjabeX.o4GjgEb7TQOMCNcx4OjtnswFpNrWBFu', 'ADMIN');

-- Demo coder accounts use password: admin123
INSERT OR IGNORE INTO users (username, email, password, role, xp, rank_title, problems_solved, battles_won, battles_lost)
VALUES ('byteknight', 'byteknight@codearena.com', '$2a$10$iyHiGiN36A2WlwBTjabeX.o4GjgEb7TQOMCNcx4OjtnswFpNrWBFu', 'CODER', 120, 'Apprentice', 3, 1, 0);

INSERT OR IGNORE INTO users (username, email, password, role, xp, rank_title, problems_solved, battles_won, battles_lost)
VALUES ('loopwizard', 'loopwizard@codearena.com', '$2a$10$iyHiGiN36A2WlwBTjabeX.o4GjgEb7TQOMCNcx4OjtnswFpNrWBFu', 'CODER', 80, 'Novice', 2, 0, 1);

INSERT OR IGNORE INTO users (username, email, password, role, xp, rank_title, problems_solved, battles_won, battles_lost)
VALUES ('stackrider', 'stackrider@codearena.com', '$2a$10$iyHiGiN36A2WlwBTjabeX.o4GjgEb7TQOMCNcx4OjtnswFpNrWBFu', 'CODER', 210, 'Specialist', 5, 2, 1);

INSERT OR IGNORE INTO problems (title, description, difficulty, category, tags, is_published, created_by)
VALUES (
    'Two Sum',
    'Given an array of integers nums and an integer target, return indices of the two numbers that add up to target.\n\nExample:\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]',
    'Easy', 'Arrays', 'array,hashmap', 1, 1
);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample)
VALUES (1, '2 7 11 15\n9', '0 1', 1);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample)
VALUES (1, '3 2 4\n6', '1 2', 0);

INSERT OR IGNORE INTO problems (title, description, difficulty, category, tags, is_published, created_by)
VALUES (
    'Reverse a String',
    'Write a method that reverses a string.\n\nExample:\nInput: hello\nOutput: olleh',
    'Easy', 'Strings', 'string', 1, 1
);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample)
VALUES (2, 'hello', 'olleh', 1);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample)
VALUES (2, 'CodeArena', 'anerAedoC', 0);

INSERT OR IGNORE INTO problems (title, description, difficulty, category, tags, is_published, created_by)
VALUES (
    'FizzBuzz',
    'Print numbers 1 to N. For multiples of 3 print Fizz, multiples of 5 print Buzz, both print FizzBuzz.\n\nExample:\nInput: 5\nOutput:\n1\n2\nFizz\n4\nBuzz',
    'Easy', 'Logic', 'loops', 1, 1
);

-- Test cases for FizzBuzz (problem_id = 3)
INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample)
VALUES (3, '5', '1\n2\nFizz\n4\nBuzz', 1);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample)
VALUES (3, '15', '1\n2\nFizz\n4\nBuzz\nFizz\n7\n8\nFizz\nBuzz\n11\nFizz\n13\n14\nFizzBuzz', 0);

INSERT OR IGNORE INTO problems (title, description, constraints, input_format, output_format, difficulty, category, tags, is_published, created_by)
VALUES (
    'Palindrome Check',
    'Read a word and print YES if it is a palindrome, otherwise print NO.',
    'Input length is between 1 and 100 characters.',
    'A single lowercase word.',
    'Print YES or NO.',
    'Easy', 'Strings', 'string,two-pointer', 1, 1
);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample, sequence_order)
VALUES (4, 'madam', 'YES', 1, 1);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample, sequence_order)
VALUES (4, 'arena', 'NO', 0, 2);

INSERT OR IGNORE INTO problems (title, description, constraints, input_format, output_format, difficulty, category, tags, is_published, created_by)
VALUES (
    'Maximum in Array',
    'Read N integers and print the largest value.',
    '1 <= N <= 1000. Values fit in a signed 32-bit integer.',
    'First line contains N. Second line contains N integers.',
    'Print the maximum integer.',
    'Medium', 'Arrays', 'array,loops', 1, 1
);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample, sequence_order)
VALUES (5, '5\n3 9 1 7 2', '9', 1, 1);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample, sequence_order)
VALUES (5, '4\n-5 -2 -9 -1', '-1', 0, 2);

INSERT OR IGNORE INTO problems (title, description, constraints, input_format, output_format, difficulty, category, tags, is_published, created_by)
VALUES (
    'Prime Counter',
    'Read N and print how many prime numbers exist from 1 to N inclusive.',
    '1 <= N <= 10000.',
    'A single integer N.',
    'Print the count of prime numbers.',
    'Hard', 'Math', 'prime,number-theory', 1, 1
);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample, sequence_order)
VALUES (6, '10', '4', 1, 1);

INSERT OR IGNORE INTO test_cases (problem_id, input, expected, is_sample, sequence_order)
VALUES (6, '30', '10', 0, 2);
