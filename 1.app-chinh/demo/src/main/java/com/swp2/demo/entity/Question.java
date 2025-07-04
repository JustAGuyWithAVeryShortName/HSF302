package com.swp2.demo.entity;

import jakarta.persistence.*;


import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "question")
public class Question {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String questionText;

        @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
        private List<Option> options = new ArrayList<>();

        public Question() {
        }

        public Question(Long id, String questionText, List<Option> options) {
                this.id = id;
                this.questionText = questionText;
                this.options = options;
        }

        public Long getId() {
                return id;
        }

        public void setId(Long id) {
                this.id = id;
        }

        public String getQuestionText() {
                return questionText;
        }

        public void setQuestionText(String questionText) {
                this.questionText = questionText;
        }

        public List<Option> getOptions() {
                return options;
        }

        public void setOptions(List<Option> options) {
                this.options = options;
        }
}