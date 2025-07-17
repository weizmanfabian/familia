import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PersonaList } from './persona-list';

describe('PersonaList', () => {
  let component: PersonaList;
  let fixture: ComponentFixture<PersonaList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PersonaList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PersonaList);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
