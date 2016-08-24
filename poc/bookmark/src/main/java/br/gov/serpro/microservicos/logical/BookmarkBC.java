package br.gov.serpro.microservicos.logical;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;

import br.gov.serpro.microservicos.model.Bookmark;

@RequestScoped
public class BookmarkBC {

	@Inject
	PersistenceHelper helper;
	
	public List<Bookmark> findAll() {
		return helper.getEntityManager().createNamedQuery("Bookmark.findAll", Bookmark.class).getResultList();
	}

	public List<Bookmark> find(String filter) {
		StringBuffer ql = new StringBuffer();
		ql.append("  from Bookmark b ");
		ql.append(" where lower(b.description) like :description ");
		ql.append("    or lower(b.link) like :link ");

		TypedQuery<Bookmark> query = helper.getEntityManager().createQuery(ql.toString(), Bookmark.class);
		query.setParameter("description", "%" + filter.toLowerCase() + "%");
		query.setParameter("link", "%" + filter.toLowerCase() + "%");

		return query.getResultList();
	}

	public Bookmark load(Long id) {
		return helper.getEntityManager().find(Bookmark.class, id);
	}

	public Bookmark insert(Bookmark body) {
		helper.getEntityManager().persist(body);
		return body;
	}

	public void delete(Long id) {
		Bookmark reference = load(id);
		if(reference == null ) throw new NotFoundException();
		helper.getEntityManager().remove(reference);
	}



}
