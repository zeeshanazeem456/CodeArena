-- Sample problems for development/testing

INSERT OR IGNORE INTO problems (title, description, difficulty, category, tags, is_published, created_by)
VALUES (
    'Two Sum',
    'Given an array of integers nums and an integer target, return indices of the two numbers that add up to target.\n\nExample:\nInput: nums = [2,7,11,15], target = 9\nOutput: [0,1]',
    'Easy', 'Arrays', 'array,hashmap', 1, 1
);

INSERT OR IGNORE INTO problems (title, description, difficulty, category, tags, is_published, created_by)
VALUES (
    'Reverse a String',
    'Write a method that reverses a string.\n\nExample:\nInput: hello\nOutput: olleh',
    'Easy', 'Strings', 'string', 1, 1
);

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
VALUES (3, '15', '1\n2\nFizz\n4\nBuzz\n6\n7\n8\nFizz\n10\nBuzz\nFizz\n13\n14\nFizzBuzz', 0);