(ns word-counter.server-test
  (:require [clojure.test :refer :all])
  (:use [word-counter.server]))

(deftest test-get-all-words
  (testing "it should walk the DOM tree to find content"
    (is (= ["1" "2" "3"]
           (get-all-words-from-body "<h1>1 2 3</h1>"))))

  (testing "it should ignore script tags"
    (is (= []
           (get-all-words-from-body "<script>Content should be ignored</script>"))))

  (testing "it should ignore whitespace"
    (is (= ["word"]
           (get-all-words-from-body "<a>     \t word \t\n</a>"))))

  (testing "it should ignore capitalization"
    (is (= ["upper" "lower"]
           (get-all-words-from-body "<a>UPPER lower</a>"))))

  (testing "it should ignore punctiation"
    (is (= ["an" "exciting" "sentence" "with" "much" "grammar"]
           (get-all-words-from-body "<a>An exciting! sentence,,,, with much grammar.</a>")))))

(deftest test-count-words
  (testing "it should count all words"
    (is (= [{:word "bye" :count 3}
            {:word "hi" :count 2}] 
           (count-words ["hi" "bye" "hi" "bye" "bye"]))))

  (testing "it should only have top 10 words"
    (is (= 10 (count (count-words (map str (range 11)))))))

  (testing "it should sort by the most frequent word first"
    (is (apply > (map :count (count-words ["hi" "bye" "hi" "bye" "bye"]))))))

(deftest test-get-counts
  (testing "it should add ids to each count"
    (is (= [0 1 2]
           (map :id (get-counts [{:url "first"} {:url "second"} {:url "third"}])))))

  (testing "it should limit to top 10 urls"
    (is (= 10
           (count (get-counts (repeat 30 {:url "something"})))))))
