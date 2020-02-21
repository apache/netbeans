class SchoolStudentRelation {
public:
   class Row {
   public:
       litesql::Field<int> student;
       litesql::Field<int> school;
       Row(const litesql::Database& db, const litesql::Record& rec=litesql::Record());
   };
   static const std::string table__;
   static const litesql::FieldType School;
   static const litesql::FieldType Student;
   static void link(const litesql::Database& db, const example::School& o0, const example::Student& o1);
   static void unlink(const litesql::Database& db, const example::School& o0, const example::Student& o1);
   static void del(const litesql::Database& db, const litesql::Expr& expr=litesql::Expr());
   static litesql::DataSource<SchoolStudentRelation::Row> getRows(const litesql::Database& db, const litesql::Expr& expr=litesql::Expr());
   template <class T> static litesql::DataSource<T> get(const litesql::Database& db, const litesql::Expr& expr=litesql::Expr(), const litesql::Expr& srcExpr=litesql::Expr());
;
;
};

template <> litesql::DataSource<example::School> SchoolStudentRelation::get(const litesql::Database& db, const litesql::Expr& expr, const litesql::Expr& srcExpr) {
   SelectQuery sel;
   sel.source(table__);
   sel.result(School.fullName());
   sel.where(srcExpr);
   return DataSource<example::School>(db, example::School::Id.in(sel) && expr);
}
template <> litesql::DataSource<example::Student> SchoolStudentRelation::get(const litesql::Database& db, const litesql::Expr& expr, const litesql::Expr& srcExpr) {
   SelectQuery sel;
   sel.source(table__);
   sel.result(Student.fullName());
   sel.where(srcExpr);
   return DataSource<example::Student>(db, example::Student::Id.in(sel) && expr);
}

litesql::DataSource<School> Student::SchoolHandle::get(const litesql::Expr& expr, const litesql::Expr& srcExpr) {
   return SchoolStudentRelation::get<School>(owner->getDatabase(), expr, (SchoolStudentRelation::Student == owner->id) && srcExpr);
}
